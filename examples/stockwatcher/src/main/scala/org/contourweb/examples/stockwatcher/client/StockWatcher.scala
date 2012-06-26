package org.contourweb.examples.stockwatcher.client

import java.util.Date

import scala.collection.mutable
import com.google.gwt.core.client.EntryPoint
import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.dom.client.ClickHandler
import com.google.gwt.event.dom.client.KeyCodes
import com.google.gwt.event.dom.client.KeyPressEvent
import com.google.gwt.event.dom.client.KeyPressHandler
import com.google.gwt.i18n.client.DateTimeFormat
import com.google.gwt.i18n.client.NumberFormat
import com.google.gwt.user.client.Random
import com.google.gwt.user.client.Timer
import com.google.gwt.user.client.Window
import com.google.gwt.user.client.ui.Button
import com.google.gwt.user.client.ui.FlexTable
import com.google.gwt.user.client.ui.HorizontalPanel
import com.google.gwt.user.client.ui.Label
import com.google.gwt.user.client.ui.RootPanel
import com.google.gwt.user.client.ui.TextBox
import com.google.gwt.user.client.ui.VerticalPanel


case class StockPrice(symbol: String, price: Double, change: Double) {
  def changePercent = 100.0 * change / price
}


class StockWatcher extends EntryPoint {
  
  private val REFRESH_INTERVAL = 5000 // ms
  private val mainPanel = new VerticalPanel
  private val stocksFlexTable = new FlexTable
  private val addPanel = new HorizontalPanel
  private val newSymbolTextBox = new TextBox
  private val addStockButton = new Button("Add")
  private val lastUpdatedLabel = new Label
  private val stocks = new mutable.ArrayBuffer[String]()

  /**
   * Entry point method.
   */
  def onModuleLoad {
    // Create table for stock data.
    stocksFlexTable.setText(0, 0, "Symbol");
    stocksFlexTable.setText(0, 1, "Price");
    stocksFlexTable.setText(0, 2, "Change");
    stocksFlexTable.setText(0, 3, "Remove");
    
    // Add styles to elements in the stock list table.
    stocksFlexTable.setCellPadding(6)
    stocksFlexTable.getRowFormatter.addStyleName(0, "watchListHeader")
    stocksFlexTable.addStyleName("watchList")
    stocksFlexTable.getCellFormatter.addStyleName(0, 1, "watchListNumericColumn")
    stocksFlexTable.getCellFormatter.addStyleName(0, 2, "watchListNumericColumn")
    stocksFlexTable.getCellFormatter.addStyleName(0, 3, "watchListRemoveColumn")
    
    // Assemble Add Stock panel.
    addPanel.add(newSymbolTextBox);
    addPanel.add(addStockButton);
    addPanel.addStyleName("addPanel")

    // Assemble Main panel.
    mainPanel.add(stocksFlexTable);
    mainPanel.add(addPanel);
    mainPanel.add(lastUpdatedLabel);
    
    // Associate the Main panel with the HTML host page.
    RootPanel.get("stockList").add(mainPanel);

    // Move cursor focus to the input box.
    newSymbolTextBox.setFocus(true);

    // Listen for mouse events on the Add button.
    addStockButton.addClickHandler(new ClickHandler {
      def onClick(event: ClickEvent) {
        addStock
      }
    })
    
    // Listen for keyboard events in the input box.
    newSymbolTextBox.addKeyPressHandler(new KeyPressHandler {
      def onKeyPress(event: KeyPressEvent) {
        if (event.getCharCode() == KeyCodes.KEY_ENTER) {
          addStock
        }
      }
    })
    
    // Setup timer to refresh list automatically.
    val refreshTimer = new Timer { 
      def run {
        refreshWatchList
      }
    }
    refreshTimer.scheduleRepeating(REFRESH_INTERVAL)
  }

  /**
   * Add stock to FlexTable. Executed when the user clicks the addStockButton or
   * presses enter in the newSymbolTextBox.
   */
  private def addStock {
    val symbol = newSymbolTextBox.getText.toUpperCase.trim
    newSymbolTextBox.setFocus(true)

    // Stock code must be between 1 and 10 chars that are numbers, letters, or dots.
    if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
      Window.alert("'" + symbol + "' is not a valid symbol.")
      newSymbolTextBox.selectAll
      return
    }

    newSymbolTextBox.setText("")

    // Don't add the stock if it's already in the table.
    if (stocks.contains(symbol))
      return

    // Add the stock to the table.
    val row = stocksFlexTable.getRowCount
    stocks += symbol
    stocksFlexTable.setText(row, 0, symbol)
    stocksFlexTable.setWidget(row, 2, new Label)
    stocksFlexTable.getCellFormatter.addStyleName(row, 1, "watchListNumericColumn")
    stocksFlexTable.getCellFormatter.addStyleName(row, 2, "watchListNumericColumn")
    stocksFlexTable.getCellFormatter.addStyleName(row, 3, "watchListRemoveColumn")

    // Add a button to remove this stock from the table.
    val removeStockButton = new Button("x", new ClickHandler {
      def onClick(event: ClickEvent) = {
        val removedIndex = stocks.indexOf(symbol)
        stocks.remove(removedIndex)
        stocksFlexTable.removeRow(removedIndex + 1)
      }
    })
    removeStockButton.addStyleDependentName("remove");
    
    stocksFlexTable.setWidget(row, 3, removeStockButton);

    refreshWatchList
  }
  
  /**
   * Generate random stock prices.
   */
  def refreshWatchList {
    val MAX_PRICE = 100.0; // $100.00
    val MAX_PRICE_CHANGE = 0.02; // +/- 2%
  
    val prices = stocks.map{ stock =>
      val price = Random.nextDouble * MAX_PRICE
      val change = price * MAX_PRICE_CHANGE * (Random.nextDouble * 2.0 - 1.0)
      StockPrice(stock, price, change)
    }

    updateTable(prices.toArray)
  }
  
  /**
   * Update the Price and Change fields all the rows in the stock table.
   *
   * @param prices Stock data for all rows.
   */
  def updateTable(prices: Array[StockPrice]) {
    prices.foreach(updateTable)
    
    // Display timestamp showing last refresh.
    lastUpdatedLabel.setText("Last update : "
        + DateTimeFormat.getMediumDateTimeFormat.format(new Date))
  }

  /**
   * Update a single row in the stock table.
   *
   * @param price Stock data for a single row.
   */
  def updateTable(price: StockPrice) {
    // Make sure the stock is still in the stock table.
    if (!stocks.contains(price.symbol)) {
      return
    }

    val row = stocks.indexOf(price.symbol) + 1

    // Format the data in the Price and Change fields.
    val priceText = NumberFormat.getFormat("#,##0.00").format(price.price)
    val changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00")
    val changeText = changeFormat.format(price.change);
    val changePercentText = changeFormat.format(price.changePercent);

    // Populate the Price and Change fields with new data.
    stocksFlexTable.setText(row, 1, priceText);
    val changeWidget = stocksFlexTable.getWidget(row, 2).asInstanceOf[Label]
    changeWidget.setText(changeText + " (" + changePercentText + "%)")
    
    // Change the color of text in the Change field based on its value.
    val changeStyleName = 
      if (price.changePercent < -0.1f)
        "negativeChange"
      else if (price.changePercent > 0.1f)
        "positiveChange"
      else
        "noChange"

    changeWidget.setStyleName(changeStyleName);
  }

}
