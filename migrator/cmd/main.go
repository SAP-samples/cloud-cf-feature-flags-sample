package main

import (
	"log"

	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/conversion"
	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/launchdarkly"
	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/parameters"
	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/sap"
)

func main() {
	sapFlags := sap.ReadFlags()
	createFlagsInLaunchDarkly(sapFlags.Flags)
}

func createFlagsInLaunchDarkly(flags []sap.Flag) {
	client := launchdarkly.NewClient(parameters.Get().ClientParams)
	failedFlags := make(map[string]error)

	for _, sapFlag := range flags {
		ldFlag, err := client.CreateFlag(conversion.ToLDFlag(sapFlag))
		if err != nil {
			failedFlags[sapFlag.ID] = err
		} else {
			conversion.SetLDTargets(sapFlag, ldFlag)
			err = client.UpdateFlagRules(ldFlag)
			if err != nil {
				failedFlags[sapFlag.ID] = err
			}
		}
	}

	for _, err := range failedFlags {
		log.Printf("[ERR] %s\n", err.Error())
	}

	allFlagsCount := len(flags)
	log.Printf("================ Summary ================\n")
	log.Printf("[INFO] %d/%d flags created successfully\n", allFlagsCount-len(failedFlags), allFlagsCount)
	log.Printf("=========================================\n")
}
