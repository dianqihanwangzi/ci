def RELEASE_BRANCH = "release-4.0"

def checkoutTiCS(branch) {
    checkout(changelog: false, poll: true, scm: [
            $class                           : "GitSCM",
            branches                         : [
                    [name: "${branch}"],
            ],
            userRemoteConfigs                : [
                    [
                            url          : "git@github.com:pingcap/tics.git",
                            refspec      : "+refs/heads/*:refs/remotes/origin/*",
                            credentialsId: "github-sre-bot-ssh",
                    ]
            ],
            extensions                       : [
                    [$class             : 'SubmoduleOption',
                     disableSubmodules  : true,
                     parentCredentials  : true,
                     recursiveSubmodules: false,
                     trackingSubmodules : false,
                     reference          : ''],
                    [$class: 'PruneStaleBranch'],
                    [$class: 'CleanBeforeCheckout'],
                    [$class: 'LocalBranch']
            ],
            doGenerateSubmoduleConfigurations: false,
    ])
    // checkout changelog: false, poll: true, scm: [$class: 'GitSCM', branches: [[name:  "${branch}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'LocalBranch'],[$class: 'CloneOption', noTags: true]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'github-sre-bot-ssh', refspec: "+refs/heads/*:refs/remotes/origin/*", url: 'git@github.com:pingcap/tics.git']]]
}

def download = { version, os, arch ->
    sh """
    wget -qnc https://download.pingcap.org/grafana-${version}.${os}-${arch}.tar.gz
    """
}

def unpack = { version, os, arch ->
    sh """
    tar -zxf grafana-${version}.${os}-${arch}.tar.gz
    """
}

def pack = { version, os, arch ->
    def tag = HOTFIX_TAG
    if (tag == "nightly") {
        tag = "master"
    }
    // use release branch to download config yaml because it was pre release and tag does not exist
    sh """
    cd "grafana-${version}"
    if [ ${tag} == "master" ] || [[ ${tag} > "v4" ]];then \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb/${RELEASE_BRANCH}/metrics/grafana/tidb.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb/${RELEASE_BRANCH}/metrics/grafana/tidb_summary.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb/${RELEASE_BRANCH}/metrics/grafana/overview.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb/${RELEASE_BRANCH}/metrics/grafana/tidb_runtime.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/pd/${RELEASE_BRANCH}/metrics/grafana/pd.json || true; \

    wget -qnc https://github.com/tikv/tikv/archive/${RELEASE_BRANCH}.zip
    unzip ${RELEASE_BRANCH}.zip
    rm -rf ${RELEASE_BRANCH}.zip
    cp tikv-*/metrics/grafana/*.json .
    rm -rf tikv-*

    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-binlog/${RELEASE_BRANCH}/metrics/grafana/binlog.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/ticdc/${RELEASE_BRANCH}/metrics/grafana/ticdc.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/monitoring/master/platform-monitoring/ansible/grafana/disk_performance.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/monitoring/master/platform-monitoring/ansible/grafana/blackbox_exporter.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/monitoring/master/platform-monitoring/ansible/grafana/node.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/monitoring/master/platform-monitoring/ansible/grafana/kafka.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/br/${RELEASE_BRANCH}/metrics/grafana/lightning.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/br/${RELEASE_BRANCH}/metrics/grafana/br.json || true; \
    cp ../metrics/grafana/* . || true; \
    else \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/tidb.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/tidb_summary.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/overview.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/pd.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/tikv_summary.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/tikv_details.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/tikv_trouble_shooting.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/performance_read.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/performance_write.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/binlog.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/disk_performance.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/blackbox_exporter.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/node.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/kafka.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/lightning.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/tiflash_proxy_summary.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/tiflash_summary.json || true; \
    wget -qnc https://raw.githubusercontent.com/pingcap/tidb-ansible/${RELEASE_BRANCH}/scripts/tiflash_proxy_details.json || true; \
    fi

    cd ..
    tiup package . -C grafana-${version} --hide --arch ${arch} --os "${os}" --desc 'Grafana is the open source analytics & monitoring solution for every database' --entry "bin/grafana-server" --name grafana --release "${HOTFIX_TAG}"
    tiup mirror publish grafana ${TIDB_VERSION} package/grafana-${HOTFIX_TAG}-${os}-${arch}.tar.gz "bin/grafana-server" --arch ${arch} --os ${os} --desc="Grafana is the open source analytics & monitoring solution for every database"
    rm -rf grafana-${version}
    """
}

def update = { version, os, arch ->
    sh """
        rm -rf ./grafana*
        """
    download version, os, arch
    unpack version, os, arch
    pack version, os, arch

}

node("build_go1130") {
    container("golang") {
        stage("Prepare") {
            println "debug command:\nkubectl -n jenkins-ci exec -ti ${NODE_NAME} bash"
            deleteDir()
        }

        checkout scm
        def util = load "jenkins/pipelines/cd/tiup/tiup_utils.groovy"

        stage("Install tiup") {
            util.install_tiup "/usr/local/bin", PINGCAP_PRIV_KEY
        }

        stage("Checkout tics") {
            def tag = HOTFIX_TAG
            if (tag == "nightly") {
                tag = "master"
            }
            if (tag == "master" || tag > "v4") {
                checkoutTiCS(RELEASE_BRANCH)
            }
        }
        

        if (ARCH_X86) {
            stage("tiup build grafana on linux/amd64") {
                update VERSION, "linux", "amd64"
            }
        }
        if (ARCH_ARM) {
            stage("TiUP build grafana on linux/arm64") {
                update VERSION, "linux", "arm64"
            }
        }
        if (ARCH_MAC) {
            stage("TiUP build grafana on darwin/amd64") {
                update VERSION, "darwin", "amd64"
            }
        }
    }
}