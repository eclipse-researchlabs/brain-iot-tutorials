---
title: Create Fabric on-premise
layout: toc-guide-page
lprev: 30-fabric-cloud.html  
lnext: ../tutorial
summary: Create Fabric using existing resources.
---

## Summary

This tutorial shows how to create a Fabric using existing compute resources.

## Setup

Each node in the fabric needs to be allowed to run **sudo** without a password and be accessible via ssh key login. It is recommended, but not required, to create a `fabric_adm` user on all nodes. The fabric deployment will create `fabric` users.
{:.note}

[Ansible][ansible] is used to manage the fabric deployment.

Choose one node from which you want to control the fabric. It doesn't have to be a fabric node, but it must have network connectivity to all other nodes in the fabric. We'll call this the `control` node.

Clone the [fabric-deployment](https://git.repository-pert.ismb.it/BRAIN-IoT/fabric-deployment){:target="_blank"} project and copy the `ansible` directory to the `control` node:

    $ git clone git@git.repository-pert.ismb.it:BRAIN-IoT/fabric-deployment.git
    $ cd fabric-deployment
    $ scp -r ansible fabric@control:.
{:.shell}

Now login to the `control` node and [install](https://docs.ansible.com/ansible/latest/installation_guide/intro_installation.html){:target="_blank"} ansible, for example:

    $ ssh user@control
    [control]$ yum install ansible
{:.shell}



### Configure nodes in fabric

Edit the `~/ansible/hosts` file to define one `infra` node and some `simple` nodes. You can also specify the `ansible_user` used to ssh into the fabric nodes.

```
[infra]
192.168.2.100 ansible_user=fabric-adm

[simple]
192.168.2.101 ansible_user=fabric-adm
192.168.2.102 ansible_user=fabric-adm
192.168.2.103 ansible_user=fabric-adm

```

The `hosts` file may be a symbolic-link to one of many fabric configurations.
{:.note}

### Configure ssh

Create a new ssh key pair called `myfabric`:

    [control]$ cd ~/ansible
    [control]$ ssh-keygen
    Generating public/private rsa key pair.
    Enter file in which to save the key (/home/fabric/.ssh/id_rsa): ./myfabric
    Enter passphrase (empty for no passphrase):
{:.shell}

Edit `ansible.cfg` and change the name of the ssh key:

```
private_key_file = myfabric
```



Configure each fabric node to allow ssh access without password, for example:

    [control]$ ssh-copy-id -i myfabric.pub fabric@node-1
    ssh-copy-id: INFO: Source of key(s) to be installed: "/home/user/ansible/myfabric_rsa.pub"
    ssh-copy-id: INFO: 1 key(s) remain to be installed -- if you are prompted now it is to install the new keys
    fabric@node-1's password:
{:.shell}

Now check that you can login without a password and check whether **sudo** requires a password:

    [control]$ ssh -i myfabric fabric@node-1
    [node-1] $ sudo id
    Password:
{:.shell}

If **sudo** asked for a password, then configure it not to ask:

    [node-1]$ sudo visudo
    Password:
    append this line to end of file
    fabric ALL=(ALL) NOPASSWD:ALL
{:.shell}


When you've done this for all the fabric nodes, you can use ansible to confirm it:

    $ ansible all --become --args id
    192.168.2.100 | CHANGED | rc=0 >>
    uid=0(root) gid=0(root) groups=0(root)
    
    192.168.2.101 | CHANGED | rc=0 >>
    uid=0(root) gid=0(root) groups=0(root)
    
    192.168.2.102 | CHANGED | rc=0 >>
    uid=0(root) gid=0(root) groups=0(root)
    
    192.168.2.103 | CHANGED | rc=0 >>
    uid=0(root) gid=0(root) groups=0(root)
{:.shell}


### Configure Java

The fabric requires Java 8.

Although openjdk-8 can be installed via the OS package manager, we've found that Oracle Java performs better on Raspberry Pi.

Download [Oracle Java 8][https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html] for Raspberry Pi (`jdk-8uXXX-linux-arm32-vfp-hflt.tar.gz`) and other Linux (`jdk-8uXXX-linux-x64.tar.gz`)

Then edit `fabric.yml` and change `jdk_version` to match the version downloaded:

```
  vars:
    fabric: brain-iot
    jdk_version: 231
    jdk_arm32: ./jdk-8u{{ jdk_version }}-linux-arm32-vfp-hflt.tar.gz
    jdk_x86_64: ./jdk-8u{{ jdk_version }}-linux-x64.tar.gz
    fabric_zip: ./fabric-eval.zip
    update_fabric: false
```

also edit `fibre.conf` and change the version in `JAVA_HOME`:

```
export JAVA_HOME="/opt/jdk1.8.0_231"
```



### Copy fabric installation resources

Copy the following fabric installation resources to `control:~/ansible`:

* fabric-eval.zip - fabric installation archive
* license.ini - fabric license file

They can be obtained from `fabric-n4` in the BRAIN-IoT demo network:

    [control]$ scp fabric-n4@fabric-n4:ansible/fabric-eval.zip ansible/.
    [control]$ scp fabric-n4@fabric-n4:ansible/license.ini ansible/.
{:.shell}

## Manage Fabric

When you have successfully deployed the fabric, you can use it for the [Distributed Deployment](20-distributed.html) tutorial.

### Deploy the Fabric

We're now ready to deploy the fabric to all nodes specified in the `hosts` file:

    [control]$ ansible-playbook fabric.yml
    ...
    PLAY RECAP **************************************************
    192.168.2.100              : ok=12   changed=1    unreachable=0    failed=0
    192.168.2.101              : ok=12   changed=1    unreachable=0    failed=0
    192.168.2.102              : ok=12   changed=1    unreachable=0    failed=0
    192.168.2.103              : ok=12   changed=1    unreachable=0    failed=0
{:.shell}

Initial fabric deployment can take 5-10 minutes depending on how many nodes are in the fabric.
{:.note}


This command only needs to be used to initially deploy the fabric code and Java to each node or when a new node is added.

It also needs to be used to update the config on all nodes, for example if you change the infrastructure node(s) in the `hosts` file or the settings in `fibre.conf`.

## Control Brain-IoT Fabric

    [control]$ ./fabric.sh
    Usage: ./fabric.sh: start | stop | erase | status | bootlog | start1 | reinstall
{:.shell}

* **start** /  **stop**  - controls the systemd `fabric.service` on all fabric nodes.
* **status** - shows Java process status on all fabric nodes.
* **erase** - removes fabric state at next start (this avoids previously deployed systems from restarting)

To perform a full restart of fabric, it is suggested you do the following:

    ./fabric.sh stop
    ./fabric.sh status
    ./fabric.sh erase
    ./fabric.sh start
    ./fabric.sh status
{:.shell}




## Trouble Shooting

The following `fabric.sh` options are useful for debugging:

* **bootlog** - shows the initial fibre startup log on each node. This will show, for example, if Java is missing.
* **start1** IP-address - this will restart the fibre service on the specified node
* **reinstall** - this will force re-install of the fabric archive, for example, if you receive an updated version.



If there is a problem with a specific fibre, there are two useful log files:

* `/opt/fabric/var/fibre.out` contains the console output of the fibre. This usually stops soon after startup when logging is redirected to `fibre.log`.
* `/opt/fabric/var/1/fibre.log` contains verbose Java logging.



## End

That completes this tutorial.

[ansible]: https://github.com/ansible/ansible