# Policy migration

This example code demonstrates how to update many policies with a command line tooling. This can be used e.g. for migration scenarios.

## Basic functionality

- Execute as command line tool
- Apply changes to policies (either provided as list or result of a search)
- Logging to console and rotated log files (migration.log*)

## Pre-requisites

The tool requires [Deno](https://deno.land/) as runtime environment.

Install it on Linux/macOS using the following command line.

      curl -fsSL https://deno.land/x/install/install.sh | sh

Install it on Windows using the following PowerShell command line.

      iwr https://deno.land/x/install/install.ps1 -useb | iex

Find more details in the Deno documentation.

## Prepare

Create/adjust a configuration file `config.yml` as a YAML file. Mandatory configuration properties are:
* WebSocket endpoint URL
* valid credentials (either username/password, bearer token or client credentials)
* migration definition (e.g. `replaceSubject`)

## Run

Run the tool to apply the changes to the policies using the following command line:

```
deno run --allow-all https://github.com/eclipse/ditto-examples/raw/master/policy-migration/src/mod.ts
```

The progress of the migration is logged. If a migration fails for a policy, the error response is logged and written to the file `failed.json`.
