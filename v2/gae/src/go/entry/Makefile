# Copyright 2011 The Snappy-Go Authors. All rights reserved.
# Use of this source code is governed by a BSD-style
# license that can be found in the LICENSE file.

include $(GOROOT)/src/Make.inc

TARG=entry
GOFILES=\
	entry.go\

export GCIMPORTS=-I../service/_obj -I../handler/_obj -I../se1/_obj -I../event/_obj -I$(GOAPPENGINEROOT)/goroot/pkg/linux_amd64/
include $(GOROOT)/src/Make.pkg

