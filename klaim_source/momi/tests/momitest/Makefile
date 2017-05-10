# Makefile for checking Klava test files

srcdir = .

all: check

JAVAJVM = java
JAVAC = javac

MOMITESTFILES = test1 test2

%.msgout: %.java
	$(JAVAJVM) momitest.$* > $@ 2>&1

check:
	rm -f *.test_output; \
	for source in $(MOMITESTFILES); do \
	  echo "executing $$source"; \
	  here=`pwd`; \
	  cd $(srcdir); \
	  if ! $(JAVAJVM) momitest.$$source >> $$source.test_output 2>&1; \
	  then errors=1; \
	   echo "error executing $$source"; fi;\
	done && cd $$here; \
	if test -z $$errors; then \
	for source in `ls *.msgout`; do \
	  test_output="`echo $$source|sed s/msgout/test_output/`"; \
	  echo "diff $(srcdir)/$$source $$test_output"; \
	  if ! diff $(srcdir)/$$source $$test_output; \
	  then errors=1; fi; \
	done && test -z $$errors; \
	else false; fi;

build-originals:
	for source in $(MOMITESTFILES); do \
	  echo "executing $$source"; \
	  here=`pwd`; \
	  cd $(srcdir); \
	  if ! $(JAVAJVM) momitest.$$source > $$source.msgout 2>&1; \
	  then errors=1; fi; \
	done && cd $$here && test -z $$errors;

clean-originals:
	rm -f *.msgout