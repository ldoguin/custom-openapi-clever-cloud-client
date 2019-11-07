# Custom Clever Cloud client generator

Download the latest codegen project and the latest API definition.

```
wget http://central.maven.org/maven2/org/openapitools/openapi-generator-cli/4.2.0/openapi-generator-cli-4.2.0.jar
wget https://api.clever-cloud.com/v2/openapi.json
```

## Go client

for of the go client templates to include oauth1 auth in ./go. Copy has been made at version 4.2.0.

`java -jar openapi-generator-cli-4.2.0.jar generate -i openapi.json -g go -o ./build/go-client -t go --skip-validate-spec`

Copy the output in the test folder

`rm -rf ./go-test/openapi cp -r ./build/go-client go-test/openapi`

Execute the test

`go run clever-request.go`

To configure the go-client oauth1 auth, please use the following environment variable:
export CC_ACCESS_TOKEN=********
export CC_ACCESS_SECRET=*******
export CC_CONSUMER_KEY==*******
export CC_CONSUMER_SECRET==*******
