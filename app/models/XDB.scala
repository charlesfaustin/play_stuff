package models

import sorm._

object XDB extends Instance(entities = Set(Entity[Music]()), url = "jdbc:h2:~/musics")