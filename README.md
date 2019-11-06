# Custom Clever Cloud client generator

Download the latest codegen project and the latest API definition.

```
wget http://central.maven.org/maven2/org/openapitools/openapi-generator-cli/4.2.0/openapi-generator-cli-4.2.0.jar
wget https://api.clever-cloud.com/v2/openapi.json
```

## Go client

for of the go client templates to include oauth1 auth.

`java -jar openapi-generator-cli-4.2.0.jar generate -i openapi.json -g go -o./ build/go-client -t go --skip-validate-spec`