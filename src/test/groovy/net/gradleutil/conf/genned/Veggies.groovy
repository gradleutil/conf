package net.gradleutil.conf.genned;

import com.typesafe.config.Optional

class Veggies {

  @Optional
  List<String> fruits  = []
  @Optional
  List<Veggie> vegetables  = []

  Veggies(){}

  Veggies from(@DelegatesTo( value = Veggies, strategy = Closure.DELEGATE_FIRST ) Closure<?> configurer ) {
    configurer.rehydrate( this, this, this ).call()
    return this
  }

}


class Veggie {

  String veggieName 
  Boolean veggieLike 

  Veggie(){}

  Veggie from(@DelegatesTo( value = Veggie, strategy = Closure.DELEGATE_FIRST ) Closure<?> configurer ) {
    configurer.rehydrate( this, this, this ).call()
    return this
  }

}



