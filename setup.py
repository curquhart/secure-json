# -*- coding: utf-8 -*-

import os
from subprocess import call
from setuptools import setup, find_packages

scriptDir=os.path.join(os.getcwd(), os.path.dirname(__file__))

with open(os.path.realpath(os.path.join(scriptDir, 'version.txt')), 'r') as fh:
    version = fh.readline()

setup(name='SecureJSON',
    version=version,
    description='Secure Java JSON Serializer and Deserializer',
    author='Chelsea Urquhart',
    author_email='chelsea.urquhart@rocketsimplicity.com',
    url='https://github.com/curquhart/secure-json',
    install_requires=[
        'javasphinx==0.9.15'
    ]
)

# Build javadocs
call(['javasphinx-apidoc', '-f', '-o', os.path.join(scriptDir, 'docs/source/'), os.path.join(scriptDir, 'src/main/java')])
