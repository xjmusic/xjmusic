| **dev** |
|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [![dev](https://github.com/xjmusic/api-spec/actions/workflows/validate.yml/badge.svg?branch=dev)](https://github.com/xjmusic/api-spec/actions/workflows/validate.yml) | 


# api-spec

OpenAPI specification for the XJ Music API.

## Swagger CLI

These specs are partial and reference a common files in the same folder. In order to export one of these specs independently, for example to view in [editor.swagger.io](https://editor.swagger.io) it is necessary to use `swagger-cli bundle` like so:

```shell
swagger-cli bundle --type yaml --dereference programs.yaml
```

The above command will copy all referenced objects, and output to stdout a self-complete yaml formatted version of the requested spec.

## Swagger Editor

Design, describe, and document OpenAPI specifications with the Swagger editor:\
https://editor.swagger.io/

## References

Specifications must comply with:\
https://swagger.io/specification/

Specifications are partly inspired by:\
https://jsonapi.org/format/
# api-spec
