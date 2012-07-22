ifeq ($(origin JAVA_HOME), undefined)
  JAVA_HOME=/usr
endif

ifneq (,$(findstring CYGWIN,$(shell uname -s)))
  JAVA_HOME := `cygpath -up "$(JAVA_HOME)"`
endif

JAVAC=$(JAVA_HOME)/bin/javac
SRCS=$(wildcard src/*.java)

NetLogoLite.jar:
	curl -f -s -S 'http://ccl.northwestern.edu/netlogo/5.0.1/NetLogoLite.jar' -o NetLogoLite.jar

table.jar table.jar.pack.gz: $(SRCS) manifest.txt NetLogoLite.jar Makefile
	mkdir -p classes
	$(JAVAC) -g -deprecation -Xlint:all -Xlint:-serial -Xlint:-path -encoding us-ascii -source 1.5 -target 1.5 -classpath NetLogoLite.jar -d classes $(SRCS)
	jar cmf manifest.txt table.jar -C classes .
	pack200 --modification-time=latest --effort=9 --strip-debug --no-keep-file-order --unknown-attribute=strip table.jar.pack.gz table.jar

table.zip: table.jar
	rm -rf table
	mkdir table
	cp -rp table.jar table.jar.pack.gz README.md Makefile src manifest.txt table
	zip -rv table.zip table
	rm -rf table
