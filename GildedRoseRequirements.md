======================================
Gilded Rose Stories
======================================

# Adding Items to Stock

I need to add Items to our stock. Every item has a name, sell-by date, and quality.
Quality is an integer always >= 0.

# Printing Stock List

I want to print the list of all items in stock.
The print should show today's date, and a list of all items with their name, number of days until sell-by, and their quality.

# Load Stock List and Print It

I want to load the stock list from disk and print it to the screen in the format that we agreed.

# View Stock List In Browser

I'm sick of the command line. I want to view the stock list in my browser.
The format should be the same as previous.

# When I View the Stock in a Browser It Should Update Qualities

Quality update is going to be complicated, but for now just reduce the quality of each item by one every day.

# Quality Should Not Fall Below 0

The Quality of an item is never negative.

# Items Degrade Faster After Their Sell-By Date

Once the sell-by date has passed items reduce in quality by two every day.

# Sulphuras Has No Sell-By Date and Doesn't Degrade

Items without a sell by date don't degrade.

# Aged Brie Gets Better With Age

It increases in quality by one every day until its sell-by date, then two after.
Its quality is capped at 50.

# Backstage Passes Increase in Value Until SellBy, Then Worthless

Quality increases by 2 when there are 10 days or less and by 3 when there are 5 days or less.
Quality drops to 0 after the concert.
Quality is capped at 50

# Quality Caps

No item can ever have a negative quality.
No item should have its quality raised above 50 by updating,
but items can be taken into stock with > 50 quality, and then degrade gradually.

# Conjured Items

Conjured items degrade in quality twice as fast as normal items.
Any item can be conjured, and if they get better with time, then conjuring makes them get better twice as fast too.
