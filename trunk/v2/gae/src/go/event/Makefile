# Copyright 2011 The Snappy-Go Authors. All rights reserved.
# Use of this source code is governed by a BSD-style
# license that can be found in the LICENSE file.

include $(GOROOT)/src/Make.inc

TARG=event
GOFILES=\
	event.go\
	factory.go\
	data.go\

export GCIMPORTS=-I../codec/_obj -I../snappy/_obj -I../se/_obj

include $(GOROOT)/src/Make.pkg

