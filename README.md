[![REUSE status](https://api.reuse.software/badge/github.com/SAP-samples/cloud-cf-feature-flags-sample)](https://api.reuse.software/info/github.com/SAP-samples/cloud-cf-feature-flags-sample)

# Migrating Feature Flags to LaunchDarkly

## Installation

1. You need to have Go > 1.16 installed on your machine.
Ensure the directory _$GOPATH/bin_ is also part of the _PATH_ environment variable.
2. Execute the following command to install the migrator tool:

```sh
go install github.com/SAP-samples/cloud-cf-feature-flags-sample@ld-flags-migrator
```

## Usage

1. Execute `cloud-cf-feature-flags-sample -h` to list the required command-line parameters.
2. Example command for migrating feature flags to LaunchDarkly:

```sh
cloud-cf-feature-flags-sample --api-key="<API-key>" --flags-file="flags.json" --project-key="default"
```
