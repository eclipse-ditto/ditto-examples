# Policy migration

This example code demonstrates how to batch update multiple policies.
 
## Basic functionality

- Execute as command line tool
- Migrate policies (either provided as list or result of a search)
- Logging to console and rotated log files (migration.log*)

## Pre-requisites

The tool requires [Deno](https://deno.land/) as runtime environment.

Install it on Linux/macOS using the following command line. Find more details in the Deno documentation.

      curl -fsSL https://deno.land/x/install/install.sh | sh

## Installation

The following statements install the tool as an OS level command `policy-migration` that executes without accessing remote module dependencies at runtime.

    deno cache https://github.com/eclipse/ditto-examples/raw/master/policy-migration/src/mod.ts

    deno install --cached-only --allow-all --unstable --force --name policy-migration https://github.com/eclipse/ditto-examples/raw/master/policy-migration/src/mod.ts

## Usage example

Create/adjust a configuration file `config.yml` as a YAML file. Mandatory configuration properties are:
* a Things WebSocket endpoint
* valid credentials (either username/password, bearer token or client credentials)
* migration definition (e.g. `replaceSubject`)

Run the tool to migrate the policies using the command line:

```
policy-migration
```

The progress of the migration is logged. If a migration fails for a policy, the error response is logged and written to the file `failed.json`.