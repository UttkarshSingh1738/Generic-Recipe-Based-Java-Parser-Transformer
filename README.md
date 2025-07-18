
## *Tasks to be verified*

> - Prepare documentation
>
>>1. List all possible match/action keys/attributes, nodeTypes etc.
>>2. Add all sub attributes if any
>>3. Add some sample syntax
>>4. Add more examples (probably list out all the currently used in the documentation)
>
> - Code Review
>
>>1. Code Review to understand the current flow
>>2. Optimize the code to make it more flexible by consolidating multiple common Actions to one Action
>
> - Error Handling (for json mapping)
>
> - Validators (Optional)
>
> - Order of json and multiple jsons to be used
> 
>>1. Create config.json where the order of the json to be executed is maintained
>>2. Add config for validators to be used and ignored
>>3. Pickup all the configs/json/output path relatively from the jar instead of expecting from the command
>
> - Create Actions as a Java Type like Match, Step etc.. instead of having it as List<Map<Map>> - might be useful for readability and additional usage
>
> - NodeMatcher Code Refactor
> 
>>1. Add flags instead of return so all the conditions will be met before moving out the matches
>>2. Populate new exception list where the conditions fail because of manual error caused in the json
>
> - Need to cover almost all scenarios by logs so that will be helpful for debugging purposes
>
> - Need to check the performance on huge codebases like having more than 10k files as well as files having more than 20k/30k lines of code
>>1. If there are huge lines of code and exception happens will it be properly logged?
>
> - Test jarTypeSolver with some sample projects to see whether we can link the classes from the jar with the current class file
>