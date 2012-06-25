package org.contourweb.common.model

import scala.collection.mutable.ArrayBuffer
import reactive._
import java.util.Calendar
import java.util.Date


trait model extends fields {
  private[model] val log = java.util.logging.Logger.getLogger("model")
  
  type AnyModel = MetaModel#Model
  type AnyField = AnyModel#Field

  def isClient: Boolean

  
  trait MetaModel extends MetaModelFields { metaModel =>
    // Type of Model required by this MetaModel to operate on
    type M <: MetaModel#Model
    
    MetaModels.add(this)
    
    
    trait Model extends BasicFields { model: M =>
      def meta = metaModel

//      locally {
//        if (fieldNames.isEmpty) {
//          TODO: Init fieldNames
//        }
//      }

      def fields: Seq[Field] = fields0
      private val fields0 = ArrayBuffer[Field]()
      
      def field(name: String) = fields.find(_.name == name)

      def sameField(someField: MetaModel#Model#Field) = field(someField.name)
      
      def validate: Map[Option[Field], List[String]] = {
        val errorMap = fields.map(field => Some(field) -> field.validate).toMap[Option[Field], List[String]]
        errorMap.filter{case (_, errors) => !errors.isEmpty}
      }
      
      override def toString = "Model "+name+"(" + fields.mkString(", ") + ")"

      private var changeStream: Option[EventSource[MetaModel#M]] = None
      
      def changes = changeStream match {
        case None =>
          changeStream = Some(new EventSource[MetaModel#M])
          changeStream.get
        case Some(stream) =>
          stream
      }
      
      def onModelChange {
        changeStream.foreach(_.fire(model))
      }
      
      def signal = new Signal[MetaModel#M] {
        def now = model // TODO: Clone?
        lazy val change: EventStream[MetaModel#M] = model.changes
        def value = model
      }


      trait Field {
        locally {
          fields0 += this
//          if (isInitialized && fields.size > fieldNames.length)
//            throw new IllegalStateException("All instances of the same Model must have a fixed number of fields")
        }

        val index = fields.size-1
        
        def name = index.toString // TODO: meta.fieldNames(index)
        
        def model: M = Model.this
        
        type T // Type of the value saved in this field
        type V // T or Option[T]

        def isOptional: Boolean
        def defaultValue: V
        
        protected var data: V = defaultValue
        
        def is = data

        def apply(v: V): M = {
          data = v
          onModelChange
          changeStream.foreach(_.fire(v))
          Model.this
        }
        def update(v: V) = apply(v) 
        
        def value: Option[T]
        def value(option: Option[T]): M
        
        private var changeStream: Option[EventSource[V]] = None
      
        def changes = changeStream match {
          case None =>
            changeStream = Some(new EventSource[V])
            changeStream.get
          case Some(stream) =>
            stream
        }

        def signal = new Signal[V] {
          def now = is // TODO: Clone?
          lazy val change: EventStream[V] = changes
        }

        def asVar = new FieldVar 
        class FieldVar extends Var[V] {
          def now = is
          def update(v: V) = Field.this.apply(v)
          lazy val change: EventStream[V] = changes
          override def toString = "FieldVar("+now+")"
        }
        
        def validate: List[String] = {
          val valids = validation :: validations ::: builtInValidations
          val errors = for (v <- valids if !v.logic()) yield v.errorMessage
          errors
        }
    
        def validation: Valid = Valid(true, "")
        def validations: List[Valid] = Nil

        protected def builtInValidations: List[Valid] = Nil

        def Valid(logic: =>Boolean, errorMessage: String) = new Valid(() => logic, errorMessage)

        class Valid(val logic: ()=>Boolean, val errorMessage: String)


        protected var dirty = false

        override def toString = ""+is
      }
    }

    
    def name = name_
    private var name_ : String = _
    
    protected var prototype: M = _

    def fields = prototype.fields

    var fieldNames: List[String] = Nil
    
    var isInitialized = false

    def init {
      prototype = create
      initialize
      isInitialized = true
    }
    protected def initialize {}
    
    // TODO: Provide default implementation
    def create: M
  }


  object MetaModels {
    private val allMap = scala.collection.mutable.Map[String,MetaModel]()

    def apply(name: String) = allMap(name)    

    def all: Seq[MetaModel] = allMap.values.toSeq
    
    private[model] def add(meta: MetaModel) = {
      allMap += (meta.name -> meta)
    }
    
    def init = all.foreach(_.init)
  }

  
  // Marker trait implemented by local and distributed stores, and maybe others
  trait StoreBase extends MetaModel
 
  case class ValidationErrors(messages: Seq[String]) extends RuntimeException  


}
