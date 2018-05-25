import os.path as path
import unittest
import subprocess
import shlex
from unittest import TestCase, skip

from server_controller import Server, Initializer, module_dir
from unsafe_client import http_get, http_post, Response

back_init = Initializer('int-test-backend-server')
front_init = Initializer('int-test-proxy-server')

h1_back_end = None
h1h1_front_end = None
h2h2_front_end = None
h2h1_front_end = None

_url_template = 'https://localhost:{}'

h1_back_port = '8443'
h1_back_url = _url_template.format(h1_back_port)

h2_back_port = '8444'
h2_back_url = _url_template.format(h2_back_port)

h1h1_front_port = '8445'
h1h1_front_url = _url_template.format(h1h1_front_port)

h2h2_front_port = '8446'
h2h2_front_url = _url_template.format(h2h2_front_port)

h2h1_front_port = '8447'
h2h1_front_url = _url_template.format(h2h1_front_port)


class TestReverseProxyServer(TestCase):

  @classmethod
  def tearDownClass(cls):
    services = [h1_back_end, h2_back_end, h1h1_front_end, h2h2_front_end, h2h1_front_end]
    for each in [each for each in services if each is not None]:
      each.kill()

  @classmethod
  def setUpClass(cls):
    print("spinning up back ends")
    back_ready_str_h1 = "Server - Started"
    back_ready_str_h2 = "netty.processId"
    global h1_back_end, h2_back_end
    h1_back_end = Server(back_init.init_script, back_ready_str_h1, False, name="h1back", port=h1_back_port).run()
    h2_back_end = Server(back_init.init_script, back_ready_str_h2, True, name="h2back", port=h2_back_port).run()

    print("spinning up front ends")
    front_ready_str = "proxy accepting connections"
    global h1h1_front_end, h2h2_front_end, h2h1_front_end
    h1h1_front_end = Server(front_init.init_script, front_ready_str, cls.get_conf('proxy_h1h1.conf'), name='h1h1proxy').run()
    h2h2_front_end = Server(front_init.init_script, front_ready_str, cls.get_conf('proxy_h2h2.conf'), name='h2h2proxy').run()
    h2h1_front_end = Server(front_init.init_script, front_ready_str, cls.get_conf('proxy_h2h1.conf'), name='h2h1proxy').run()


  @classmethod
  def get_conf(cls, name: str):
    return path.abspath(path.join(module_dir, name))

  def check_response(self, response: Response, method: str, backend: str):
    self.assertEqual(backend, response.headers['x-tag'])
    self.assertEqual(method, response.headers['x-method'])
    self.assertEqual('echo', response.headers['x-echo'])
    self.assertEqual({'title': 'Release', 'description': 'the Kraken'}, response.json_body)
    self.assertEqual(200, response.status)

  # region h1:h1

  # @skip
  def test_backend_server_get_h1_works(self):
    response = http_get(url=h1_back_url, headers={"x-echo": "echo"}, h2=False)
    self.check_response(response, 'GET', 'h1back')


  # @skip
  def test_backend_server_post_h1_works(self):
    response = http_post(url=h1_back_url, data={"key": "value"},
                         headers={"x-echo": "echo"}, h2=False)
    self.check_response(response, 'POST', 'h1back')

  # @skip
  def test_proxy_get_h1_h1(self):
    responses = [
      http_get(url=h1h1_front_url, headers={"x-echo": "echo"}, h2=False),
      http_get(url=h1h1_front_url, headers={"x-echo": "echo"}, h2=False),
    ]
    for response in responses:
      self.check_response(response, 'GET', 'h1back')

  # @skip
  def test_proxy_post_h1_h1(self):
    responses = [
      http_post(url=h1h1_front_url, data={"key": "value"}, headers={"x-echo": "echo"}, h2=False),
      http_post(url=h1h1_front_url, data={"key": "value"}, headers={"x-echo": "echo"}, h2=False),
    ]
    for response in responses:
      self.check_response(response, 'POST', 'h1back')

  # endregion

  # region h1:h2

  # @skip
  def test_proxy_get_h2_h1(self):
    responses = [
      http_get(url=h2h1_front_url, headers={"x-echo": "echo"}, h2=True),
      http_get(url=h2h1_front_url, headers={"x-echo": "echo"}, h2=True),
    ]
    for response in responses:
      self.check_response(response, 'GET', 'h1back')

  # @skip
  def test_proxy_post_h2_h1(self):
    responses = [
      http_post(url=h2h1_front_url, data={"key": "value"}, headers={"x-echo": "echo"}, h2=True),
      http_post(url=h2h1_front_url, data={"key": "value"}, headers={"x-echo": "echo"}, h2=True),
    ]
    for response in responses:
      self.check_response(response, 'POST', 'h1back')

  # endregion

  # region h2:h2

  # @skip
  def test_backend_server_get_h2_works(self):
    responses = [
      http_get(url=h2_back_url, headers={"x-echo": "echo"}, h2=True),
      http_get(url=h2_back_url, headers={"x-echo": "echo"}, h2=True),
    ]
    for response in responses:
      self.check_response(response, 'GET', 'h2back')

  # @skip
  def test_backend_server_post_h2_works(self):
    responses = [
      http_post(url=h2_back_url, data={"key": "value"}, headers={"x-echo": "echo"},
                h2=True),
      http_post(url=h2_back_url, data={"key": "value"}, headers={"x-echo": "echo"},
                h2=True),
    ]

    for response in responses:
      self.check_response(response, 'POST', 'h2back')

  # @skip
  def test_proxy_get_h2_h2(self):
    responses = [
      http_get(url=h2h2_front_url, headers={"x-echo": "echo"}, h2=True),
      http_get(url=h2h2_front_url, headers={"x-echo": "echo"}, h2=True),
    ]
    for response in responses:
      self.check_response(response, 'GET', 'h2back')

  # @skip
  def test_proxy_post_h2_h2(self):
    responses = [
      http_post(url=h2h2_front_url, data={"key": "value"}, headers={"x-echo": "echo"}, h2=True),
      http_post(url=h2h2_front_url, data={"key": "value"}, headers={"x-echo": "echo"}, h2=True),
    ]
    for response in responses:
      self.check_response(response, 'POST', 'h2back')

  # endregion

  # region load

  def test_proxy_h2_h2(self):
    out = str(subprocess.check_output(shlex.split("h2load -n1000 -c10 -t4 https://localhost:{}".format(h2h2_front_port))))
    for line in out.split('\n'):
      print(line)
      if line.startswith('requests:'):
        print("-----------------------")

  # endregion


if __name__ == '__main__':
  unittest.main()
