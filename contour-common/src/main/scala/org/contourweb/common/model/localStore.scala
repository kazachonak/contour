package org.contourweb.common.model

import collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import reactive._


trait localStore extends model {
  private[localStore] val log = java.util.logging.Logger.getLogger("localStore")
  
  trait LocalStore extends StoreBase { meta =>
    type M <: LocalStore#Model

    private val store = BufferSignal[M]()
    private var lastId = 0

    trait Model extends super.Model { model: M =>

      val id = new StringField {
        override def unique = true
      }
      
      def isNew = id.is.isEmpty

      def save {
        val errors = validate
        if (!errors.isEmpty)
          throw new ValidationErrors(errors.values.toList.flatten)
        if (id.is.isEmpty) {
          id() = lastId.toString
          lastId += 1
          store.value += this
        } else {
          store.value.update(store.value.indexOf(this), this)
        }
      }
      
      def delete = {
        store.value -= this
      }

      class StringField extends super.StringField {
        def unique = false

        override protected def builtInValidations =
          Valid(isUnique, "Already exists") :: super.builtInValidations
//          Valid(isUnique, model.meta.name+" with "+name+" = "+is+" already exists")

        def isUnique = {
          val similarFields = all.now.flatMap(_.sameField(this))
          ! similarFields.exists(other => this.is == other.is && this != other)
        }
      }
    }


    def findAll: Seq[M] = store.value

    def all: SeqSignal[M] = store
  }

  
}
