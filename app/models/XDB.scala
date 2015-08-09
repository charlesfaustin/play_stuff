package models

import sorm._

object XDB extends Instance(entities = Set(Entity[Music]()) + Entity[CrtdFile](), url = "jdbc:postgresql://localhost/ubuntu",
                                          user = "ubuntu",
                                          password = "password",
                                          initMode = InitMode.Create)
