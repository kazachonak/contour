package org.contourweb.examples.stockwatcher2

import java.util.Date
import org.contourweb.client.model.client
import org.contourweb.client.view.gwtView
import org.contourweb.common.model.localStore
import com.google.gwt.i18n.client.DateTimeFormat
import com.google.gwt.user.client.Random
import com.google.gwt.user.client.ui.TextBox
import reactive.Timer


object stockWatcher extends localStore with client with gwtView {

  
class Stock extends Stock.Model {

  val symbol = new StringField {
    override def apply(s: String) = super.apply(s.toUpperCase.trim)
    override def unique = true
    override def validation =
      Valid(is.matches("^[0-9A-Z\\.]{1,10}$"), "'" + is + "' is not a valid symbol.")
  }
  
  val price = new DoubleField {
    override def format = "#,##0.00"
  }
  
  val change = new DoubleField {
    override def format = "+#,##0.00;-#,##0.00"
  }
  
  val changePercent = new DoubleField {
    override def is = 100.0 * change / price
    override def format = "+#,##0.00;-#,##0.00"
  }
}

  
object Stock extends MetaModel with LocalStore {
  type M = Stock
  def create = new Stock // TODO: Remove it
}


class StockWatcherWindow extends GwtRootWindow {
  def main = new StockWatcherView
}


class StockWatcherView extends View {
  import Widgets._
  
  private val REFRESH_INTERVAL = 5000 // ms
  private val newStock = new Stock
  private def dateNow = DateTimeFormat.getMediumDateTimeFormat.format(new Date)
  private var newSymbolTextBox: TextBox = _
  
  val widget = VerticalPanel()(
    FlexTable(Stock.all, cellPadding=6, css="watchList", headerCss="watchListHeader")( stock => List(
      Column("Symbol")(Label(stock.symbol)),
      Column("Price", css="watchListNumericColumn")(Label(stock.price.toString)),
      Column("Change", css="watchListNumericColumn")(
        // Change the color of text in the Change field based on its value.
        Label(stock.change.toString + " (" + stock.changePercent.toString + "%)",
              css = if (stock.changePercent < -0.1f)     "negativeChange"
                    else if (stock.changePercent > 0.1f) "positiveChange"
                    else                                 "noChange"
        )
      ),
      Column("Remove", css="watchListRemoveColumn")(
        Button(stock.delete, "x", dependentCss="remove")
      )
    )),
    Form(onSubmit = addStock)(implicit form =>
      HorizontalPanel(css="addPanel")(
        TextBoxValid(newStock.symbol, onEnter = form.submit, init = newSymbolTextBox=_),
        Button(form.submit, "Add")
      )
    ),
    Label(Stock.all.deltas.map(_ => "Last updated: "+dateNow).hold(""))
  )

  override def onLoad {
    // Move cursor focus to the input box.
    newSymbolTextBox.setFocus(true)
    
    // Setup timer to refresh list automatically.
    new Timer(0, REFRESH_INTERVAL).foreach(_ => refreshWatchList)
  }

  private def addStock {
    newSymbolTextBox.setFocus(true)
    Stock.create.symbol(newStock.symbol).save
    newStock.symbol() = ""
    refreshWatchList
  }
  
  /**
   * Generate random stock prices.
   */
  def refreshWatchList {
    val MAX_PRICE = 100.0 // $100.00
    val MAX_PRICE_CHANGE = 0.02 // +/- 2%

    Stock.all.now.foreach{ stock =>
      stock.price(Random.nextDouble * MAX_PRICE)
           .change(stock.price * MAX_PRICE_CHANGE * (Random.nextDouble * 2.0 - 1.0))
           .save
    }
  }
}


}
