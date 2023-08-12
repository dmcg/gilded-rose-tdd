Gilded Rose Stories
===================

## ✓ Adding Items to Stock

I need to add Items to our stock. Every item has a name, sell-by date, and quality.
Quality is an integer always >= 0.

## ✓ Printing Stock List

I want to print the list of all items in stock.
The print should show today's date, and a list of all items with their name, number of days until sell-by, and their quality.

## ✓ Load Stock List and Print It

I want to load the stock list from disk and print it to the screen in the format that we agreed.

## ✓ View Stock List In Browser

I'm sick of the command line. I want to view the stock list in my browser.
The format should be the same as previous.

## ✓ When I View the Stock in a Browser It Should Update Qualities

Quality update is going to be complicated, but for now just reduce the quality of each item by one every day.

## ✓ Quality Should Not Fall Below 0

The Quality of an item is never negative.

## ✓ Items Degrade Faster After Their Sell-By Date

Once the sell-by date has passed items reduce in quality by two every day.

## ✓ Sulphuras Has No Sell-By Date and Doesn't Degrade

Items without a sell by date don't degrade.

## ✓ Aged Brie Gets Better With Age

It increases in quality by one every day until its sell-by date, then two after.
Its quality is capped at 50.

## ✓ Backstage Passes Increase in Value Until SellBy, Then Worthless

Quality increases by 2 when there are 10 days or less and by 3 when there are 5 days or less.
Quality drops to 0 after the concert.
Quality is capped at 50

## ✓ Quality Caps

No item can ever have a negative quality.
No item should have its quality raised above 50 by updating,
but items can be taken into stock with > 50 quality, and then degrade gradually.

## ✓ Conjured Items

Conjured items degrade in quality twice as fast as normal items.
Any item can be conjured, and if they get better with time, then conjuring makes them get better twice as fast too.

## ✓ Identify Individual Items

Magical items are more like used cars than cartons of milk - their identity is important.
We might bring three items with the same name, quality and sell-by into stock, but need to know which individual one we sold.
Sometimes items have a serial number or distinguishing feature that we can use as identifier. Where they don't, we can attach a label with a code to an item.

## ✓ Display Item Price From Value Elf

Computers can't determine the value of magical items, that requires magic. There are three online interfaces to the required magic, we eventually need to ask all three.

For now though, just get the price from Value Elf and display it in the stock list screen.

## ✓ Speed Up Stock Listing

Listing items was fast enough, but now that we are adding prices it's too slow (~3s). Can we make it under half a second?

## ✗ Display Item Price From Multiple Sources

Fetch the price from Price-O-Matic and WeBuyAnyMagicalItem as well as Value Elf.

If two or more agree use that value, otherwise the average. If any service is down (or doesn't respond in time) use those that aren't.

Display the price in the stock list screen.

Superceded - ValueElf turns out to be giving us good enough prices

## ✓ Remove Items from the Stock List

We are currently reflecting (and updating the quality in) an Excel TSV file that I am editing by hand.

I'd like a way to remove items from the stock list through the software.

## ✓ Add Items to the Stock List

I'd like a way to add new items when they come into stock.

## ✓ Prevent Accidental Item Deletion

I accidentally deleted an item the other day. Can we stop that from happening?

## ☐ Reduce the Value Elf Service Charges

Inflation has increased Elf wages - can we reduce the bill?

## ☐ Show our Stock List on the Internet

## ☐ Let people buy on the Internet

## ☐ Edit Items in the Stock List



