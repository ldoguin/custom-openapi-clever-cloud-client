package main

import (
	"context"
	"fmt"

	"./openapi"
)

func main() {
	config := openapi.NewConfiguration()
	// create the API client, with the transport
	client := openapi.NewAPIClient(config)
	var err error
	var user openapi.UserView
	user, _, err = client.DefaultApi.GetUser1(context.Background())

	fmt.Printf("Raw Response Body:\n%v\n", user)
	if err != nil {
		panic(err.Error())
	}

}
