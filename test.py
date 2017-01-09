#!/usr/bin/python
import os
import sys
from stat import *
import zmq
import threading
import multiprocessing
import time
import math
import socket
import signal
import argparse
from urlparse import urlparse
import shutil
import tempfile
import subprocess
import re
import codecs
import ConfigParser
import linuxcnc
from machinekit import service
from machinekit import config

from google.protobuf.message import DecodeError
from machinetalk.protobuf.message_pb2 import Container
from machinetalk.protobuf.config_pb2 import *
from machinetalk.protobuf.types_pb2 import *
from machinetalk.protobuf.status_pb2 import *
from machinetalk.protobuf.preview_pb2 import *
from machinetalk.protobuf.motcmds_pb2 import *
from machinetalk.protobuf.object_pb2 import ProtocolParameters

# reload(sys)  
# sys.setdefaultencoding('utf8')

print "default encoding: " + sys.getdefaultencoding()


context = zmq.Context()
socket = context.socket(zmq.ROUTER)

identity = "worker12"
socket.identity = identity.encode('utf8')
uri = "tcp://machinekit.local:6202"
socket.connect(uri)

cont = Container()

cont.type = MT_PING

txBuffer = cont.SerializeToString()
socket = context.socket(zmq.DEALER)

socket.setsockopt(zmq.RCVTIMEO, 1000)
socket.setsockopt(zmq.LINGER, 10)

print "sending txBuffer: " + txBuffer
socket.send_multipart([identity + txBuffer], zmq.NOBLOCK)
#socket.send(txBuffer)

cont.clear()

