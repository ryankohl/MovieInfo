MovieInfo
=========

Web app presented at SemTechBiz 2012 tutorial.

Currently an early version that depends on RDF being served up on local (Fuseki) triple stores as such:

localhost:3030/ds - extract of DBpedia
localhost:3031/ds - extract of LMDB

The app is optimized to deploy to a Tomcat 7.0 container, at localhost:8080/MovieInfo

The point of this code is to demonstrate a few techniques for looking around RDF stores and defining the content
you want to extract.  Each tab has a pair of corresponding servlets to support a piece of functionality:

Survey - Send a 'survey query' to an endpoint to get a count of triples by class and property
Probe - Look at the available data for a random sample (default size: 10) of class instances from a source endpoint.
Extract - Define construct queries to pull the content you want into the shape you want.  Queries and resulting RDF/XML 
files are stored locally on the server.
Integrate - Load ontologies and Jena rules files to the server, accept a request to survey the combination of the 
extracted RDF, loaded ontologies, and rules files, and execute SELECT queries on the model.
Web App - A really ugly example of what the extracted data could support.  Pie charts!