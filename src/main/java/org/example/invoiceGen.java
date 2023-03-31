package org.example;

import com.github.javafaker.Faker;

import peppol.bis.invoice3.api.*;
import peppol.bis.invoice3.domain.*;
import peppol.bis.invoice3.validation.ValidationResult;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class invoiceGen {


    public static void main(String[] args) {
        for (int i =100; i < 250; i++) {

        Faker faker = new Faker(new Locale("en-AU"));
        ABNGenerator abnGenerator = new ABNGenerator();
        String supplier = faker.company().name();
        String supplierABN = abnGenerator.generateRandomABN();
        com.github.javafaker.Address supplierAdrs = faker.address();
        String supplierStreetNumber = supplierAdrs.streetAddressNumber();
        String supplierStreetName = supplierAdrs.streetName();
        String supplierCity = supplierAdrs.city();
        String supplierZipCode = supplierAdrs.zipCode();
        String supplierState = supplierAdrs.state();
        String supplierFirstName = faker.name().firstName();
        String supplierLastName = faker.name().lastName();
        String supplierPhone = faker.phoneNumber().cellPhone();
        String companySuffix = faker.company().suffix();
        String supplierFullName = supplier + " " + companySuffix;
        String supplierEmail = supplierFirstName + "." + supplierLastName + "@" + supplier.replaceAll("\\s+", "-") + ".com.au";

        String customer = faker.company().name();
        String customerABN = abnGenerator.generateRandomABN();
        com.github.javafaker.Address customerAdrs = faker.address();
        String customerStreetName = customerAdrs.streetName();
        String customerStreetNumber = customerAdrs.streetAddressNumber();
        String customerCity = customerAdrs.city();
        String customerZipCode = customerAdrs.zipCode();
        String customerState = customerAdrs.state();
        String customerFirstName = faker.name().firstName();
        String customerLastName = faker.name().lastName();
        String customerPhone = faker.phoneNumber().cellPhone();
        String customerFullName = customer + " " + companySuffix;
        String customerEmail = customerFirstName + "." + customerLastName + "@" + customer.replaceAll("\\s+", "-") + ".com.au";

        final AccountingSupplierParty accountingSupplierParty = new AccountingSupplierParty(
                new Party(
                        new EndpointID(supplierABN).withSchemeID("0151"),
                        new PostalAddress(new Country("AU"))
                                .withStreetName(supplierStreetNumber + supplierStreetName)
                                .withCityName(supplierCity)
                                .withPostalZone(supplierZipCode)
                                .withCountrySubentity(supplierState),

                        new PartyLegalEntity(supplierFullName).withCompanyID(new CompanyID(supplierABN).withSchemeID("0151"))
                ).withPartyIdentification(new PartyIdentification(new ID(supplierABN).withSchemeID("0151")))
                        .withPartyName(new PartyName(supplier))
                        .withPartyTaxScheme(new PartyTaxScheme(supplierABN, new TaxScheme("GST")))
                        .withContact(
                                new Contact()
                                        .withName(supplierFirstName + " " + supplierLastName)
                                        .withTelephone(supplierPhone)
                                        .withelectronicMail(supplierEmail)
                        )
        );

        final AccountingCustomerParty accountingCustomerParty = new AccountingCustomerParty(
                new Party(
                        new EndpointID(customerABN).withSchemeID("0151"),
                        new PostalAddress(new Country("AU"))
                                .withStreetName(customerStreetName)
                                .withCityName(customerCity)
                                .withPostalZone(customerZipCode)
                                .withCountrySubentity(customerState)

                        , new PartyLegalEntity(customerFullName).withCompanyID(new CompanyID(customerABN).withSchemeID("0151"))
                ).withPartyIdentification(new PartyIdentification(new ID(customerABN).withSchemeID("0151")))

                        .withPartyName(new PartyName(customer))
                        .withPartyTaxScheme(new PartyTaxScheme(customerABN, new TaxScheme("GST")))
                        .withContact(
                                new Contact()
                                        .withName(customerFirstName + " " + customerLastName)
                                        .withTelephone(customerPhone)
                                        .withelectronicMail(customerEmail)
                        )
        );




        Random rand = new Random();
        double amount = 100 + rand.nextDouble() * (20000 - 100);

        double lineAmount = Math.round(amount * 10) / 10.0;
        String lineAmountString = String.format("%.2f", lineAmount);


        // Calculate the tax amount as 10% of the taxable amount
        double taxAmount = lineAmount * 0.1;


        final TaxTotal taxTotal = new TaxTotal(
                new TaxAmount(String.format("%.2f", taxAmount), "AUD")
        ).withTaxSubtotal(
        new TaxSubtotal(
                new TaxableAmount(String.format("%.2f", lineAmount), "AUD"),
                new TaxAmount(String.format("%.2f", taxAmount), "AUD"),
                new TaxCategory("S", new TaxScheme("GST")).withPercent("10")
        )
    );

        double taxInclusiveAmount = lineAmount + taxAmount;
        String finalAmount = String.format("%.2f", taxInclusiveAmount);
        final LegalMonetaryTotal legalMonetaryTotal = new LegalMonetaryTotal(
                new LineExtensionAmount(String.format("%.2f", lineAmount), "AUD"),
                new TaxExclusiveAmount(String.format("%.2f", lineAmount), "AUD"),
                new TaxInclusiveAmount(String.format("%.2f", taxInclusiveAmount), "AUD"),
                new PayableAmount(finalAmount, "AUD")
        ).withAllowanceTotalAmount(new AllowanceTotalAmount("0.00", "AUD"))
                .withChargeTotalAmount(new ChargeTotalAmount("0.00", "AUD"))
                .withPrepaidAmount(new PrepaidAmount("0.00", "AUD"));

        String itemName = faker.commerce().productName();
        String itemDescription = faker.commerce().material() + " " + itemName + " with " + faker.commerce().color();
        String sellersItemId = faker.number().digits(6);
        String itemNote = faker.commerce().promotionCode();




        final InvoiceLine invoiceLine = new InvoiceLine(
                "1", new InvoicedQuantity("1", "E99"),
                new LineExtensionAmount(lineAmountString, "AUD"),

                new Item(
                        itemName,
                        new ClassifiedTaxCategory("S", new TaxScheme("GST")).withPercent("10")
                )
                        .withDescription(itemDescription)
                        .withSellersItemIdentification(new SellersItemIdentification(sellersItemId)),

                new Price(new PriceAmount(lineAmountString, "AUD"))).withNote(itemNote);



        final Delivery delivery = new Delivery().withDeliveryLocation(
                new DeliveryLocation().withAddress(
                        new Address(new Country("AU")).withAdditionalStreetName(customerStreetNumber + customerStreetName)
                                .withCityName(customerCity)
                                .withPostalZone(customerZipCode)
                )
        ).withDeliveryParty(new DeliveryParty(new PartyName(customerFullName)));


        //generate a random payment id 12 digits long
        String paymentID = String.format("%012d", new Random().nextInt(1000000000));

        final PaymentMeans paymentMeans1 = new PaymentMeans(
                new PaymentMeansCode("30")
                        .withName("Credit transfer")
        )
                .withPaymentID(paymentID)
                .withPayeeFinancialAccount(
                        new PayeeFinancialAccount("ACCOUNTNUMBER")
                                .withFinancialInstitutionBranch(new FinancialInstitutionBranch("BSB")
                                )
                                .withName("ACCOUNTNAME")
                );


        String invoiceID = String.format("%08d", new Random().nextInt(100000000));
        String invoiceDate = LocalDate.now().toString();
        String dueDate = LocalDate.ofEpochDay(ThreadLocalRandom.current().nextLong(LocalDate.parse(invoiceDate).toEpochDay(), LocalDate.parse(invoiceDate).plusDays(30).toEpochDay())).toString();

        // PayeeParty
        String payeeFirstName = faker.name().firstName();
        String payeeLastName = faker.name().lastName();
        String payeeFullName = payeeFirstName + " " + payeeLastName;
        String payeeABN = abnGenerator.generateRandomABN();

    // TaxRepresentativeParty
        String taxRepFirstName = faker.name().firstName();
        String taxRepLastName = faker.name().lastName();
        String taxRepFullName = taxRepFirstName + " " + taxRepLastName;
        com.github.javafaker.Address taxRepAdrs = faker.address();
        String taxRepStreetName = taxRepAdrs.streetName();
        String taxRepAdditionalStreetName = taxRepAdrs.secondaryAddress();
        String taxRepCity = taxRepAdrs.city();
        String taxRepZipCode = taxRepAdrs.zipCode();
        String taxRepState = taxRepAdrs.state();

        final Invoice invoice = new Invoice(
                invoiceID
                , invoiceDate
                , "AUD"
                , accountingSupplierParty
                , accountingCustomerParty
                , taxTotal
                , legalMonetaryTotal
                , Collections.singletonList(invoiceLine)
            ).withBuyerReference("n/a")
                .withDueDate(dueDate)
                .withPaymentTerms(new PaymentTerms("30 days from invoice date"))
                .withDelivery(delivery)
                .withPaymentMeans(paymentMeans1)
                .withPayeeParty(
                        new PayeeParty(new PartyName(payeeFullName))
                                .withPartyIdentification(new PartyIdentification(new ID(payeeABN).withSchemeID("0151")))
                                .withPartyLegalEntity(new PayeePartyPartyLegalEntity(new CompanyID(payeeABN).withSchemeID("0151")))
                )
                .withTaxRepresentativeParty(
                        new TaxRepresentativeParty(
                                new PartyName(taxRepFullName),
                                new PostalAddress(new Country("AU"))
                                        .withStreetName(taxRepStreetName)
                                        .withAdditionalStreetName(taxRepAdditionalStreetName)
                                        .withCityName(taxRepCity)
                                        .withPostalZone(taxRepZipCode)
                                        .withCountrySubentity(taxRepState),
                                new PartyTaxScheme(payeeABN, new TaxScheme("GST"))
                        )
                );

//
        final PeppolBillingApi<Invoice> api = PeppolBillingApi.create(invoice);
        String content = api.prettyPrint().replaceAll("urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0",
                "urn:cen.eu:en16931:2017#conformant#urn:fdc:peppol.eu:2017:poacc:billing:international:aunz:3.0");

        try {
            FileWriter fileWriter = new FileWriter(i + ".xml");
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        }

    }
}




