# Google Scholar Profiles Parser

Google Scholar Parser is the desktop app which extracts data from [Google Scholar](https://scholar.google.com.ua/).
Extracted data:
* User name
* User id
* Page link
* Citations
* H-index
* Presence

## Application Usage

Simply enter url for page of Google Scholar with list of users, e.g. lecturers from
[University of Toronto](https://scholar.google.com/citations?view_op=view_org&hl=en&org=8515235176732148308) and click "parse".

Parsing speed is about 1 user/sec and University of Toronto Google Scholar page have about 3000 users, so it will take some time. To simply check work of this app, you can click "Stop" button after parsing start, or enter [last page](https://scholar.google.com.ua/citations?view_op=view_org&hl=en&org=8515235176732148308&after_author=oEy3AP3___8J&astart=4590) of users from the same university.

Insert url into edit field and click "Parse" button.


<a href="url"><img src="https://github.com/VladNamik/ScholarParser/blob/master/screenshots/on_parsing.png?raw=true" align="center" height="396" width="565"></a>

When parsing ends you can save received data in xls format.


<a href="url"><img src="https://github.com/VladNamik/ScholarParser/blob/master/screenshots/parsing_finished.png?raw=true" align="center" height="396" width="565"></a>

If you already used this app and want to update the available data, you can use "Update" button with xls file you have.
