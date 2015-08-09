from fabric.api import env, local, run, sudo

import os

env.user = 'ubuntu'
env.key_filename = ['~/.ssh/chefbox.pem']


env.hosts = ['52.18.159.108']
project_root = "~/"


#  hardcoded for now
repo_root = os.path.join(project_root, 'play_stuff')


def update():
    sudo('apt-get -y update', pty=True)


def upgrade():
    sudo('apt-get -y upgrade', pty=True)


def update_supervisor():
    sudo('supervisorctl reread && supervisorctl update')


def install_nginx():
    sudo('apt-get install nginx -y')


def restart_ngninx():
    sudo('service nginx restart')


def os_packages():
    sudo('apt-get install -y openjdk-7-jdk  sox libsox-fmt-mp3 unzip git supervisor nginx', pty=True)
    run('echo "deb http://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list')
    sudo('apt-get update')
    sudo('apt-get install -y  sbt  --force-yes')
    sudo('apt-get install  -y scala')


def install_activator():
    run('cd ~/')
    run('wget http://downloads.typesafe.com/typesafe-activator/1.3.5/typesafe-activator-1.3.5.zip')
    run('unzip typesafe-activator-1.3.5.zip')
    run('cd /usr/bin && sudo ln -s /home/ubuntu/activator-dist-1.3.5/activator activator')
    


def clone():
    run('cd ~/')
    run('git clone https://github.com/charlesfaustin/play_stuff.git')
    run('cd play_stuff/ && git checkout music')



def pull():
    run('cd %s && git pull' % repo_root)


def clean():
    #run('cd %s && activator clean' % repo_root )
    run(' activator clean' )



def code_compile():
    run('cd %s && activator compile' % repo_root)


def runserver():
    run('cd %s && activator run' % repo_root)


def stage():
    run('cd %s && activator clean compile stage' % repo_root)



def make_logs():
    run("touch /home/ubuntu/play_app.log")
    run("touch /home/ubuntu/nginx-access.log")
    run("touch /home/ubuntu/nginx-error.log")


def nginx_conf():
    sudo("cp %s /etc/nginx/sites-available" % (repo_root + '/deploy/slyck'))
    try:
        sudo("rm /etc/nginx/sites-enabled/default")
    except:
        pass
    sudo("ln -s /etc/nginx/sites-available/slyck /etc/nginx/sites-enabled/slyck")


def prod_run():
    run(' %s/target/universal/stage/bin/hey' %repo_root)

def supervisor_conf():
    sudo("cp %s /etc/supervisor/conf.d" % (repo_root + '/deploy/slyck.conf'))


def nginx_restart():
    sudo('service nginx restart')


def app_restart():
    sudo("supervisorctl restart slyck")


def initial_install():
    update()
    upgrade()
    os_packages()
    install_activator()
    clone()
    pull()
    stage()
    nginx_conf()
    supervisor_conf()
    update_supervisor()
    app_restart()
    nginx_restart()






