package models

import sorm._

object XDB extends Instance(entities = Set(Entity[Music]()) + Entity[CrtdFile](), url = "jdbc:h2:~/musics")