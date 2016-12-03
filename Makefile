default:
	@cmd/setup
	@cmd/build

setup:
	@cmd/setup

build:
	@cmd/build

.PHONY: ui
ui:
	@cmd/build -u

clean:
	@cmd/clean

distclean:
	@cmd/clean -d

ideaclean:
	@cmd/clean -i
