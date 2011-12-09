# Copyright 2011 The Snappy-Go Authors. All rights reserved.
# Use of this source code is governed by a BSD-style
# license that can be found in the LICENSE file.

include $(GOROOT)/src/Make.inc

TARG=snappy
GOFILES=\
	decode.go\
	encode.go\
	snappy.go\

export GCIMPORTS=-I../codec/_obj
include $(GOROOT)/src/Make.pkg

