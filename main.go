package main

import (
	"log"
	"strings"

	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/errors"

	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/conversion"
	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/launchdarkly"
	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/parameters"
	"github.com/SAP-samples/cloud-cf-feature-flags-sample/pkg/sap"
)

func main() {
	sapFlags := sap.ReadFlags()
	ldClient := launchdarkly.NewClient(parameters.Get().ClientParams)
	checkForFlagCollisions(ldClient, sapFlags.Flags)
	createFlagsInLaunchDarkly(ldClient, sapFlags.Flags)
}

func checkForFlagCollisions(client *launchdarkly.Client, flags []sap.Flag) {
	ldFlagIDs, err := client.GetLDFlagKeys()
	errors.Check(err)
	conflictingFlags := make([]string, 0)
	for _, sapFlag := range flags {
		if _, ok := ldFlagIDs[sapFlag.ID]; ok {
			conflictingFlags = append(conflictingFlags, sapFlag.ID)
		}
	}
	if len(conflictingFlags) != 0 {
		log.Fatalf("[ERR] Found %d conflicting flags with keys: %s",
			len(conflictingFlags), strings.Join(conflictingFlags, ", "))
	}
}

func createFlagsInLaunchDarkly(client *launchdarkly.Client, flags []sap.Flag) {
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
