package models

import sorm._

object XDB extends Instance(entities = Set(Entity[Person]()), url = "jdbc:h2:~/uwotuwot")