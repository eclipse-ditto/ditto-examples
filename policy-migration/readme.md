# Policy migration

This example code demonstrates how to update many policies with a command line
tooling. This can be used e.g. for migration scenarios.

## Basic functionality

- Execute as command line tool
- Apply changes to policies (either provided as list or result of a search)
- Logging to console and rotated log files (migration.log*)

## Pre-requisites

The tool requires [Deno](https://deno.land/) as runtime environment.

Find more details on how to install Deno in your environment in the
[Deno documentation](https://deno.land/#installation).

## Prepare

Create/adjust a configuration file `config.yml` as a YAML file from the template
`config.template.yml` which also documents the available configuration
properties. Mandatory configuration properties are:

- HTTP endpoint URL
- valid credentials (either username/password, bearer token or client
  credentials)
- migration definition (e.g. `replaceSubject`)

## Run

Run the tool to apply the changes to the policies using the following command
line:

```
deno run --allow-read=config.yml,migration.log,failed.json --allow-write=migration.log,failed.json --allow-net https://github.com/eclipse/ditto-examples/raw/master/policy-migration/src/mod.ts
```

The progress of the migration is logged. If a migration fails for a policy, the
error response is logged and written to the file `failed.json`.
