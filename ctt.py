#! python
from sys import argv,exit
import xml.etree.ElementTree as ET
import subprocess
import os
import difflib
import filecmp

# Sources: http://stackoverflow.com/questions/16275402/ignoring-lines-while-comparing-files-using-python
# https://docs.python.org/2/library/difflib.html

class FileComparator(object):
  """ Helper class to compare files considering filters """
  def __init__(self, file1, file2, filters):
    # check if files exist
    if not os.path.exists(file1):
      raise Exception("File " + file1 + " does not exist")
    if not os.path.exists(file1):
      raise Exception("File " + file2 + " does not exist")
    self.left = file1
    self.right = file2
    self.filters = filters

  def compare(self):
    return filecmp.cmp(self.left, self.right, shallow=False)

class Output(object):
  """ Base class for expected output """
  DEFAULT_FILTERS = []
  def __init__(self, expected, filters = None):
    self.expected = expected
    self.filters = filters.split(",") if filters else Output.DEFAULT_FILTERS
    
class FileOutput(Output):
  """ inner class with simple fields """
  def __init__(self, generated, expected, filters = None):
    super(FileOutput, self).__init__(expected, filters)
    self.generated = generated
    
  def __str__(self):
    result = "Generated: " + self.generated + ", expected: " + self.expected
    return result + ", filters: " + ", ".join(self.filters)

class StdoutOutput(Output):
  """ inner class with simple fields """
  def __init__(self, expected, filters = None):
    super(FileOutput, self).__init__(expected, filters)
    
  def __str__(self):
    result = "Expected: " + self.expected
    return result + ", filters: " + ", ".join(self.filters)

    
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
      default_filters = output.attrib.get("filters")
      files = output.findall("{https://github.com/fourier/ctt}file")
      for f in files:
        filters = f.attrib.get("filters")
        result.append(FileOutput(f.attrib.get("generated"), f.attrib.get("expected"), 
                             filters if filters else default_filters))
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
    stderr = stderr.split("\n") if stderr else []
    stdout = runner.stdout.read()
    stdout = stdout.split("\n") if stdout else []
    if errorcode != 0:
      raise TestRunner.TestRunnerException(self, "Performed with error code " + str(errorcode), stdout, stderr)
    # compare files
    self.compare(directory, stdout, stderr)

  def compare(self, basedir, stdout, stderr):
    output_files = self.config.output_files()
    for o in output_files:
      gen = o.generated
      if not os.path.isabs(gen): gen = os.path.join(basedir, gen)
      test = o.expected
      if not os.path.isabs(test): test = os.path.join(basedir, test)
      try:
        comp = FileComparator(test, gen, o.filters)
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

  print("Done")

if __name__ == '__main__':
  main()
