pipeline {
    agent any

    stages {
        stage('Checking commands') {
            steps {
                sh '''#!/bin/bash
                    type oc
                    type helm
                    type git
                    type make
                    type docker
                    type podman
                    pwd
                    cat /etc/os-release
                    dnf install -y make
                    set
                    id
                '''
            }
        }
        stage('Configuring cluster') {
            steps {
                sh '''#!/bin/bash
                    echo "---------------------------------------------------------"
                    echo "EXECUTION_COMMAND:${EXECUTION_COMMAND}"
                    tmpdir=$(mktemp -d)
                    echo "${CLUSTER_BOT_AUTHENTICATION}" > ${tmpdir}/cluster_bot.txt
                    pushd "${tmpdir}"
                    echo "${tmpdir}" > /tmp/TMPDIR.txt
                    git clone https://github.com/latchset/tang-operator
                    pushd tang-operator/tools/cluster_tools
                    source ./add_cluster.sh -c admin -f ${tmpdir}/cluster_bot.txt
                    oc get nodes
                    echo "---------------------------------------------------------"   
                '''
            }
        }
        stage('Executing Test Suite') {
            steps {
                sh '''#!/bin/bash
                    echo "---------------------------------------------------------"
                    curl http://ftp.gnu.org/gnu/make/make-4.4.tar.gz > /tmp/make-4.4.tar.gz
                    tmpdir=$(cat /tmp/TMPDIR.txt)
                    cp /tmp/make-4.4.tar.gz $tmpdir
                    pushd $tmpdir
                    tar -zxvf make-4.4.tar.gz
                    cd make-4.4
                    ./configure
                    sh build.sh
                    readlink -f ./make
                    echo "PATH:${PATH}"
                    oc get nodes
                    "$EXECUTION_COMMAND"
                    echo "---------------------------------------------------------"   
                '''
            }
        }
    }
}
