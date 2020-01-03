package main

import (
	"context"
	"fmt"

	openapi "./clevercloud"
)

func filterAvailableInstanceView(ss []openapi.AvailableInstanceView, test func(openapi.AvailableInstanceView) bool) (ret []openapi.AvailableInstanceView) {
	for _, s := range ss {
		if test(s) {
			ret = append(ret, s)
		}
	}
	return ret
}

func main() {
	config := openapi.NewConfiguration()
	// create the API client, with the transport
	client := openapi.NewAPIClient(config)
	var err error
	var user openapi.UserView
	user, _, err = client.SelfApi.GetUser(context.Background())

	fmt.Printf("Raw Response Body:\n%v\n", user)
	if err != nil {
		panic(err.Error())
	}
	var zones []openapi.ZoneView
	zones, _, err = client.ProductsApi.GetZones(context.Background())

	fmt.Printf("Raw Response Body:\n%v\n", zones)
	if err != nil {
		panic(err.Error())
	}

	instanceTest := func(instance openapi.AvailableInstanceView) bool {
		return instance.Type == "docker" && instance.Enabled
	}

	var availableInstances []openapi.AvailableInstanceView
	var dockerInstance openapi.AvailableInstanceView

	availableInstances, _, err = client.ProductsApi.GetAvailableInstances(context.Background(), nil)
	dockerInstance = filterAvailableInstanceView(availableInstances, instanceTest)[0]

	fmt.Printf("Raw Response Body:\n%v\n", dockerInstance)
	if err != nil {
		panic(err.Error())
	}

	var app openapi.ApplicationView
	app, _, err = client.SelfApi.AddSelfApplication(context.Background(), openapi.WannabeApplication{
		Name:            "regis",
		Description:     "desc",
		Zone:            "par",
		InstanceType:    dockerInstance.Type,
		MinInstances:    1,
		MaxInstances:    1,
		MinFlavor:       "XS",
		MaxFlavor:       "XS",
		Deploy:          "git",
		InstanceVersion: dockerInstance.Version,
	})

	fmt.Printf("Raw Response Body:\n%v\n", app)
	if err != nil {
		panic(err.Error())
	}
}
