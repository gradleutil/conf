package net.gradleutil.conf.bean


import net.gradleutil.conf.annotation.Optional
import net.gradleutil.conf.annotation.ToStringIncludeNames

import static net.gradleutil.conf.util.ConfUtil.setBeanFromConfigFile

@ToStringIncludeNames
class Manytyped {

    Manytyped(){ }

    Manytyped(File conf, File confOverride){
        setBeanFromConfigFile(this, conf, confOverride)
    }

	Type type
	enum Type  {
		VANILLA("vanilla"),FORGE("forge"),SPIGOT("spigot"),PAPER("paper")

		private final String name;

		private Type(String s) {
			name = s;
		}

		public boolean equalsName(String otherName) {
			return name.equals(otherName);
		}

		public String toString() {
			return this.name;
		}
	}


	@Optional
    String _1funkyProperty

    Boolean isCool

    Action action

    String dollarstart

    String someNull

    Long max

    String description

    List<ToDo> todos

    List<String> aSimpleStringList

    List<Task> tasks

}

@ToStringIncludeNames
class Action {

    Action(){ }

    SubAction subAction

    String name

    String type

}

@ToStringIncludeNames
class SubAction {

    SubAction(){ }

    String name

    String action

    SubSubAction subSubAction

}

@ToStringIncludeNames
class SubSubAction {

    SubSubAction(){ }

    String subname

}

@ToStringIncludeNames
class ToDo {

    ToDo(){ }

    String name

    String type

}

@ToStringIncludeNames
class Task {

    Task(){ }

    String name

    String type

}
