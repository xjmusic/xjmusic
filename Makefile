### DEFINITIONS

.setup:
	@cmd/setup

.build:
	@cmd/build

.clean:
	@cmd/clean

.distclean:
	@cmd/clean -d

.ideaclean:
	@cmd/clean -i

### COMMANDS

.PHONY: setup build clean distclean ideaclean

setup: .setup

build: .build

clean: .clean

distclean: .distclean

ideaclean: .ideaclean
