pragma solidity >=0.4.22 <0.6.0;

contract Mortal {
    /* Define variable owner of the type address */
    address owner;

    /* This constructor is executed at initialization and sets the owner of the contract */
    constructor() public { owner = msg.sender; }

    /* Function to recover the funds on the contract */
    function kill() public { if (msg.sender == owner) selfdestruct(msg.sender); }
}

contract Greeter is Mortal {

    /* Define contract name of the type string */
    string name;

    /* this runs when the contract is executed */
    constructor(string memory _name) public {
        name = _name;
    }

    /* Main function */
    function getContractName() public view returns (string memory) {
        return name;
    }

    /* send function */
    function greet(address _recipient, string memory _message) public {
        Greeter g = Greeter(_recipient);
        g.receive(name, _message);
    }

    /* receive function */
    function receive(string calldata _name, string calldata _message) external {
        emit MessageReceived(msg.sender, _name, _message);
    }

    /* example event in solidity */
    event MessageReceived(address indexed sender, string name, string message);
}