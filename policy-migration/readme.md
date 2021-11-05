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
- migration steps (e.g. `replaceSubject`, `addSubject`, `addEntry`)

### Migration steps

The following migration steps can be configured:

#### replaceSubject

Replaces an existing subject in all entries. Checks for existance of the old subject i.e. does not add if the old one does not exist.

Configuration example:
```
migrations:
  - replaceSubject:
      old: "the existing subject to replace"
      new: "the new subject that replaces the existing one"
      type: "the type of the new subject"
```

#### addSubject

Adds a new subject to the entry with the given label.

Configuration example:
```
migrations:
  - addSubject:
    label: "the label of the policy entry for which to add the subject"
    subject: "the new subject that is added to the entry"
    type: "the type of the new subject"
```

#### addEntry

Adds a new entry to the policy with the given label. If `replace: true` is given, existing entries are replaced, otherwise existing entries are preserved.

Configuration example:
```
migrations:
  - addEntry:
    label: "the label of the added policy entry"
    entry: | 
    {
      subjects: { theSubject: { type: "theType" } },
      resources: { "thing:/": { grant: ["READ", "WRITE"], revoke: []} },
    }
    replace: true | false
```


## Run

Run the tool to apply the changes to the policies using the following command
line:

```
deno run --allow-read=config.yml,migration.log,failed.json --allow-write=migration.log,failed.json --allow-net https://github.com/eclipse/ditto-examples/raw/master/policy-migration/src/mod.ts
```

The progress of the migration is logged. If a migration fails for a policy, the
error response is logged and written to the file `failed.json`.