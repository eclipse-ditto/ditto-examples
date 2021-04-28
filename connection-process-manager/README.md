
# Connection Process Manager

This example code demonstrates how to execute and trigger command line processes based on information of 
managed connections in an Eclipse Ditto installation.

As an example, it could be used to establish SSH tunnels to all the target endpoints of managed connections inside Ditto.

## Basic functionality

- Execute as command line tool
- Retrieve connection information using Eclipse Ditto DevOps commands
- Prepare command line by using the connection information as [Mustache](https://mustache.github.io/) placeholders in a 
  provided command line pattern
- Execute commands as sub-processes
- Makes sure that commands are always running and are re-started when they are stopped or when the connection 
  information has changed
- Logging to console and rotated log files (connection-process-monitor.log*)

## Pre-requisites

The tool requires [Deno](https://deno.land/) as runtime environment.

Install it on Linux/macOS using the following command line. Find more details in the Deno documentation.

      curl -fsSL https://deno.land/x/install/install.sh | sh

## Installation

The following statements install the tool as an OS level command `connection-process-manager` that executes without 
accessing remote module dependencies at runtime.

    deno cache https://github.com/eclipse/ditto-examples/raw/master/connection-process-manager/src/mod.ts

    deno install --cached-only --allow-all --unstable --force --name connection-process-manager https://github.com/eclipse/ditto-examples/raw/master/connection-process-manager/src/mod.ts

## Usage example
Create a script (e.g. `tunnel.sh`) that contains the logic to actually establishing a SSH tunnel:

    #!/bin/bash
    trap 'kill $(jobs -p)' EXIT
    export SSHPASS=$2
    sshpass -e ssh -N -o ServerAliveInterval=60 $1@$3 -p $4 -R 8080:localhost:8080

Create/adjust a configuration file `config.yml` as a YAML file. Use the script in the `cmdPattern` configuration section.

Run the tool to establish the SSH tunnels for each single connection using the command line:

    connection-process-manager
