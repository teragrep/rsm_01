# Build a libtool library, libJavaLognorm.la.
pkglib_LTLIBRARIES = libJavaLognorm.la
libJavaLognorm_la_SOURCES = JavaLognorm.c
libJavaLognorm_la_CPPFLAGS = -I$(top_srcdir) $(PTHREADS_CFLAGS) $(RSRT_CFLAGS) $(JAVA_INCLUDES) $(shell pkg-config --cflags lognorm)
libJavaLognorm_la_LDFLAGS = -module -avoid-version $(JAVA_LIBS) $(shell pkg-config --libs lognorm)