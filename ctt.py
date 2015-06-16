#! python
from sys import argv,exit
import xml.etree.ElementTree as ET
import subprocess
import os
import difflib
import filecmp
import re
from itertools import izip, ifilter

# Sources: http://stackoverflow.com/questions/16275402/ignoring-lines-while-comparing-files-using-python
# https://docs.python.org/2/library/difflib.html

class FileComparator(object):
  """ Helper class to compare files considering ignore_regexp """
  def __init__(self, file1, file2, ignore_regexp):
    # check if files exist
    if not os.path.exists(file1):
      raise Exception("File " + file1 + " does not exist")
    if not os.path.exists(file1):
      raise Exception("File " + file2 + " does not exist")
    self.left = file1
    self.right = file2
    self.ignore_regexp = ignore_regexp

  def compare(self):
    if self.ignore_regexp == None:
      return filecmp.cmp(self.left, self.right, shallow=False)
    else:
      p = re.compile(self.ignore_regexp)
      with open(self.left) as f1, open(self.right) as f2:
        f1 = ifilter(lambda x: not(p.search(x)), f1)
        f2 = ifilter(lambda x: not(p.search(x)), f2)
        return all(x == y for x, y in izip(f1, f2))

class Expected(object):
  """ Base class for expected output """
  def __init__(self, expected, ignore_regexp = None):
    self.expected = expected
    self.ignore_regexp = ignore_regexp
    
class FileExpected(Expected):
  """ inner class with simple fields """
  def __init__(self, generated, expected, ignore_regexp = None):
    super(FileExpected, self).__init__(expected, ignore_regexp)
    self.generated = generated
    
  def __str__(self):
    result = "Generated: " + self.generated + ", expected: " + self.expected
    return result + ", ignore_regexp: " + "'" + self.ignore_regexp + "'"

class StdOutExpected(Expected):
  """ inner class with simple fields """
  def __init__(self, expected, fname = None, ignore_regexp = None):
    super(StdOutExpected, self).__init__(expected, ignore_regexp)
    self.fname = None
    if fname: self.fname = fname
    
  def __str__(self):
    result = "Expected: " + self.expected
    return result + ", ignore_regexp: " + "'" + self.ignore_regexp + "'"

    
class TestRun(object):
  """ Describes the test run configuration """            
  def __init__(self, element):
    """ element is a test-run xml element """
    self.element = element
    self.name = element.attrib["name"]
    self.cmdline = element.find("{https://github.com/fourier/ctt}command-line")
    if self.cmdline == None:
      raise Exception("Test Run " + self.name + " doesn't have command-line node")
    
  def command_line(self):
    """ returns the command line constructed for the testrun """
    executable = self.cmdline.attrib["executable"]
    args = [x.text for x in self.cmdline.findall("{https://github.com/fourier/ctt}argument")]
    args.insert(0,executable)
    return args

  def directory(self):
    """ returns the directory where to perform testrun """
    # directory is optional
    pwd = self.cmdline.get("directory")
    if pwd == None:
      return "."
    return pwd

  def output_files(self):
    result = []
    for output in self.element.findall("{https://github.com/fourier/ctt}output"):
      default_ignore_regexp = output.attrib.get("ignore")
      files = output.findall("{https://github.com/fourier/ctt}file")
      for f in files:
        ignore_regexp = f.attrib.get("ignore")
        result.append(FileExpected(f.attrib.get("generated"), f.attrib.get("expected"), 
                             ignore_regexp if ignore_regexp else default_ignore_regexp))
    return result

  def output_stdout(self):
    result = None
    stdout = self.element.find("{https://github.com/fourier/ctt}stdout")
    if stdout is not None:
      result = StdOutExpected(stdout.text,
                              stdout.attrib.get("file"),
                              stdout.attrib.get("ignore"))
    return result

      
class Configuration(object):
  """ Parse the configuration defined in xml file """
  def __init__(self, filename):
    tree = ET.parse(filename)
    self.root = tree.getroot()
  def get_testruns(self):
    result = []
    for tr in self.root.findall("{https://github.com/fourier/ctt}test-run"):
      result.append(TestRun(tr))
    return result


class TestRunner(object):
  """ The object which actually performs the test run """
  class TestRunnerException(Exception):
    """ Custom exception """
    def __init__(self, runner, reason, stdout = None, stderr = None):
      super(TestRunner.TestRunnerException,self).__init__(reason)
      self.message = reason
      self.name = runner.config.name
      self.stdout = stdout
      self.stderr = stderr
      
  def __init__(self, tr):
    self.config = tr

  def remove_generated_files(self):
    """ Remove generated files to clean the test run """
    for output in self.config.output_files():
      try:
        os.remove(output.generated)
      except OSError,e:
        None

  def explain(self):
    print("Test run: " + self.config.name)
    print(" - Command line: " + " ".join(self.config.command_line()))
    print(" - Directory: " + self.config.directory())
    output_files = self.config.output_files()
    print(" - Files: ")
    for o in output_files: print("    - " + str(o))   
  
  def perform(self):
    """ execute the test run """
    #self.explain()
    # first remove all old generated files for this testrun
    self.remove_generated_files()
    # prepare an absolute path to the directory where
    # we are going to execute the command
    directory = os.path.abspath(self.config.directory())
    if not os.path.exists(directory):
      raise TestRunner.TestRunnerException(self, " Directory doesn't exist: " + directory)
    # run the process
    try:
      runner = subprocess.Popen(self.config.command_line(), cwd = directory, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
      runner.wait()
    except Exception, e:
      raise TestRunner.TestRunnerException(self, str(e))
    errorcode = runner.returncode
    stderr = runner.stderr.read()
    stderr = stderr.splitlines() if stderr else []
    stdout = runner.stdout.read()
    stdout = stdout.splitlines() if stdout else []
    if errorcode != 0:
      raise TestRunner.TestRunnerException(self, "Performed with error code " + str(errorcode), stdout, stderr)
    # compare stdout
    self.compare_stdout(directory, stdout, stderr)
    # compare files
    self.compare_files(directory, stdout, stderr)

  def compare_stdout(self, directory, stdout, stderr):
    expect = self.config.output_stdout()
    expected_lines = []
    if not expect:
      return
    exc = TestRunner.TestRunnerException(self, "Output do not match", stdout, stderr)
    # get expected lines
    if expect.fname:
      fname = os.path.join(directory, expect.fname) if not os.path.abspath(expect.fname) else expect.fname
      if not os.path.exists(fname):
        raise TestRunner.TestRunnerException(self, "File " + fname + " does not exist")
      expected_lines = open(fname,"r").read().splitlines()
      exc = TestRunner.TestRunnerException(self, "Output do not match to contents of the " + fname, stdout, stderr)
    else:
      expected_lines = expect.text.splitlines()
    # now comparte with stdout
    if len(stdout) != len(expected_lines):
      raise exc
    for i in range(len(stdout)):
      if stdout[i] != expected_lines[i]:
        raise exc

  def compare_files(self, basedir, stdout, stderr):
    output_files = self.config.output_files()
    for o in output_files:
      gen = o.generated
      if not os.path.isabs(gen): gen = os.path.join(basedir, gen)
      test = o.expected
      if not os.path.isabs(test): test = os.path.join(basedir, test)
      try:
        comp = FileComparator(test, gen, o.ignore_regexp)
        if not comp.compare():
          raise TestRunner.TestRunnerException(self, "Files " + gen + " and " + test + " does not match", stdout, stderr)
      except Exception,e:
        raise TestRunner.TestRunnerException(self, e)
    
def usage(app_name):
  print("Usage: " + app_name + " config_file.xml")
  print("Where config_file.xml is any ctt configuration file");

  
def main():
  # verify arguments
  if len(argv) == 1:
    usage(argv[0])
    exit(1)
  # create configuration object per configuration file
  for arg in argv[1:]:
    cfg = Configuration(arg)
    # get test runs from the config
    test_runs = cfg.get_testruns()
    for tr in test_runs:
      # execute test run
      try: 
        runner = TestRunner(tr)
        print("Running \"" + runner.config.name + "\"")
        runner.perform()
      except TestRunner.TestRunnerException, e:
        print("Test Run \"" + e.name + "\", error: " + str(e))
        if e.stdout:
          print("Output:")
          for l in e.stdout: print(l)
        if e.stderr:
          print("Error output:")
          for l in e.stderr: print(l)
        exit(1)

if __name__ == '__main__':
  main()
