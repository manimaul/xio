import subprocess
import sys
import functools
import os.path as path
from threading import Thread
from queue import Queue, Empty

module_dir = path.abspath(path.join(path.dirname(__file__)))
_root_dir = path.abspath(path.join(module_dir, '..'))


class StdOutReader:
  def __init__(self, stream, verbose=False):
    self._stream = stream
    self._queue = Queue()
    self._verbose = verbose

    def _reader(s, queue):
      while True:
        line = s.readline()
        s.flush()
        if line:
          queue.put(line)

    self._thread = Thread(target=_reader, args=(self._stream, self._queue))
    self._thread.daemon = True
    self._thread.start()

  def readline(self):
    try:
      line = str(self._queue.get(block=False, timeout=0.1))
      if line and self._verbose:
        print(line)
      return line
    except Empty:
      return ''


class Initializer:
  def __init__(self, project):
    cmd = self._cmd_for_task(project, 'assembleDist', 'installDist')
    print("gradle cmd: {}".format(cmd))
    project_module_dir = path.abspath(path.join(_root_dir, project))
    self._init_script = path.join(project_module_dir,
                                  'build/install/{}/bin/{}'.format(project, project))
    if subprocess.call(cmd, shell=True) == 0 and path.exists(self._init_script):
      print('assembleDist installDist success')
    else:
      print('assembleDist installDist failed')
      sys.exit(1)

  def _cmd_for_task(self, project, *tasks):
    template = ':{}:{}'
    t = functools.reduce(lambda a, b: ("", template.format(project, a) + " " + template.format(project, b)), tasks)[1]
    return "{}/gradlew -p {} {}".format(_root_dir, _root_dir, t)

  @property
  def init_script(self):
    return self._init_script


class Server:
  def __init__(self, script, ready_str, *args, **kwargs):
    self._port = kwargs.get('port', '8443')
    host = kwargs.get('host', '')
    self._name = kwargs.get('name', 'unnamed')
    self._verbose = kwargs.get('verbose', False)
    if len(args) > 1:
      argv = functools.reduce(lambda a, b: ("", str(a).strip() + " " + str(b).strip()), args)[1]
    elif len(args) is 1:
      argv = str(args[0]).strip()
    else:
      argv = ''
    self.cmd = "{} {} {} {} {}".format(script, host, self.port, self.name, argv)
    self.process = None
    self.ready_str = ready_str

  @property
  def port(self):
    return str(self._port)

  @property
  def name(self):
    return str(self._name)

  def run(self):
    if self.process is None:
      print("server start cmd: {}".format(self.cmd))
      self.process = subprocess.Popen("exec " + self.cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
      nb_err = StdOutReader(self.process.stderr, verbose=self._verbose)
      nb_out = StdOutReader(self.process.stdout, verbose=self._verbose)
      while True:
        if self.ready_str in nb_err.readline() or self.ready_str in nb_out.readline():
          break
    return self

  def kill(self):
    if self.process is not None:
      self.process.kill()
