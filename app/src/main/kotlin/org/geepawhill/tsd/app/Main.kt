package org.geepawhill.tsd.app

import javafx.stage.Stage
import tornadofx.App
import tornadofx.launch

class Main : App() {

    override fun start(stage: Stage) {
        super.start(stage)
        stage.show()
    }

}

fun main(args: Array<String>) {
    launch<Main>(args)
}