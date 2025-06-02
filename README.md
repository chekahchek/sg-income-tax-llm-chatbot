## Singapore income tax chatbot
LLM chatbot to answer questions regarding tax relief in Singapore. 

Information is scraped from the IRAS website, and stored in a Postgres database.
Based on the user's input, appropriate information is retrieved from the database and 
send to OpenAI LLM to generate a response.

## Setup
1. Create an OpenAI API key and store it as an environment variable.

```
export API_KEY=your_api_key
```

2.  Spin up Postgres database from docker image.

```
docker-compose up --build
```

3. Run ingestion/Vectorisation.scala to scrape and store the documents in the database


## Example usage
```
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: text/plain" \
  -d "your message here"
```


