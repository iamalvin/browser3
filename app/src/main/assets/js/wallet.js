//adding event receivers
function newWalletEvent(event){
    alert(JSON.stringify(event));
}

function setSeedEvent(event){
    alert(JSON.stringify(event));
}

function clearAddresses(){
    $(".address").innerHTML = "";
}

function refreshAddresses(){
    var gai = browser3.global_address_info;
    $(".address").html("");
    $(".userAddr").html("");

    var coinbase_text = "Coinbase: " + browser3.coinbase;
    $("#coinbase").html(coinbase_text);

    for (var addr in gai){
        var addr_info = " <div> "
                      + " <b>address</b>: " + browser3.truncateAddress(gai[addr].address)
                      + " <b>Balance</b>: [" + gai[addr].balance + " ETH]"
                      + " <b>Nonce</b>: " + gai[addr].nonce
                      + " </div> ";

        $(".address").append(addr_info);

        var addrAsOption = '<option value="' + gai[addr].address + '">'
                                + browser3.truncateAddress(gai[addr].address) + ' with balance [' + gai[addr].balance + ' ETH] ' +
                           '</option>';

        $(".userAddr").append(addrAsOption);
    }
}

//finished adding event receivers

$(document).ready(function(){

    //adding event Listeners

    browser3.addListener("newWallet", newWalletEvent);
    browser3.addListener("setSeed", setSeedEvent);
    browser3.addListener("getting addresses", clearAddresses);
    browser3.addListener("global address info updated", refreshAddresses);

    //finished adding event listeners
    refreshAddresses();

    window.setInterval(refreshAddresses, 15000);

    if (browser3.global_addresses.length <= 0){
        $('#loading').hide();
        $('#no-wallet').show();
        $('#wallet').hide();

        document.getElementById("newVault").onclick = function (){
            console.log("creating Vault");
            var password = prompt('Enter password for encryption, remember it, once lost it can not be recovered!', 'password');
            browser3.lightwallet.keystore.createVault({password: password}, function(err, ks){
                ks.keyFromPassword(password, function(err, pwDerivedKey){
                    ks.generateNewAddress(pwDerivedKey, 1);
                    console.log(ks.getAddresses());

                    var serialized_keystore = ks.serialize();
                    browser3FullKeyStore.saveKeyStore(serialized_keystore);
                    window.location.reload();
                });
            })
        }

    } else {
        $('#loading').hide();
        $('#no-wallet').hide();
        $('#wallet').show();

        $(".scanForAddress").click(function(){
            b3JSI.scanForAddress();
        });

        $("#sendEth").click(function(){
            var fromAddr = $("#sendFrom").val();
            var toAddr = $("#sendTo").val();
            var value = $("#sendAmount").val();
            var weiValue = value * (10 ** 18);
            var gasPrice = $("#gasPrice").val();
            var weiGasPrice =  gasPrice * (10 ** 9);
            var gas = $("#gas").val();
            web3.eth.sendTransaction(
            {   from: fromAddr,
                to: toAddr,
                value: weiValue,
                gasPrice: weiGasPrice,
                gas: gas
            }, function (err, txhash) {
                if(err) console.log('error: ' + err);
                if(txhash) console.log('txhash: ' + txhash);
                //b3JSI.saveTransaction(fromAddr, toAddr, weiValue, gas, weiGasPrice);
            });
        });


        $("#functionCall").click(function(){
            var contractCaller = $("#contractCaller").val();
            var contractAddr = $("#contractAddr").val();
            var contractAbi = JSON.parse($("#contractAbi").val());
            var contract = web3.eth.contract(abi).at(contractAddr);
            var functionName = $("#functionName").val();
            var args = JSON.parse('[' + document.getElementById('functionArgs').value + ']');
            var value = $("#contractSendAmount").val();
            var weiValue = value * (10 ** 18);
            var gasPrice = $("#contractGasPrice").val();
            var weiGasPrice =  gasPrice * (10 ** 9);
            var gas = $("#contractGas").val();

            args.push({from: fromAddr, value: value, gasPrice: gasPrice, gas: gas})
            var callback = function(err, txhash) {
                alert('error: ' + err)
                alert('txhash: ' + txhash)
            }

            args.push(callback);
            contract[functionName].apply(this, args);
        });
    }
});