# jstruct

`jstruct` is a Java package to add simple data model validation to Java. It works in a way similar to the fantastic [`Pydantic`](https://docs.pydantic.dev/latest/) Python package.

## Supports
* Recursive serialization and deserialization using Jackson
* Model validation using Jakarta (you need to include a call to `BaseModel.modelValidate()` in your constructor)
* Simple to add to existing classes
* Primed for use with SLF4J

## Disclosure
I wrote this in a morning a day after starting to learn Java, so its more of a fun project than production ready.
