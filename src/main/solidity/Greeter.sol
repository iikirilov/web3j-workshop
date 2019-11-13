pragma solidity ^0.4.2;

// Modified Greeter contract. Based on example at https://www.ethereum.org/greeter.

contract Mortal {
    /* Define variable owner of the type address*/
    address owner;

    /* this function is executed at initialization and sets the owner of the contract */
    constructor() public { owner = msg.sender; }

    /* Function to recover the funds on the contract */
    function kill() public { if (msg.sender == owner) selfdestruct(owner); }
}

contract Greeter is Mortal {

    string name;

    /* this runs when the contract is executed */
    constructor(string _name) public {
        name = _name;
    }

    /* send function */
    function greet(address _recipient, string _message) public {
        Greeter g = Greeter(_recipient);
        g.receive(name, _message);
    }

    /* receive function */
    function receive(string _name, string _message) external {
        emit MessageReceived(msg.sender, _name, _message);
    }

    /* example event in solidity */
    event MessageReceived(address indexed sender, string name, string message);
}
