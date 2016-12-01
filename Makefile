default:
	@cmd/setup
	@cmd/build

setup:
	@cmd/setup

.PHONY: ui
ui:
	@cmd/build -u

build:
	@cmd/build

clean:
	@cmd/clean

distclean:
	@cmd/clean -d

ideaclean:
	@cmd/clean -i

install:
	@cmd/install

package:
	@cmd/package
