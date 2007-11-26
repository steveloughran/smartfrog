/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.smartfrog.services.amazon.restbookch13;

/**
 * Amazon S3 client application. Returns a list of buckets.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class S3App extends S3Resource {

    public S3App(String awsID, String secretKey) {
        super(awsID, secretKey);
    }

    public S3App(S3Resource parent) {
        super(parent);
    }

    /*
    public static void main(String... args) {
        for (S3Bucket bucket : new S3App().getBuckets()) {
            System.out.println(bucket.getName() + " : " + bucket.getUri());
        }
    }

    public List<S3Bucket> getBuckets() {
        List<S3Bucket> result = new ArrayList<S3Bucket>();

        // Fetch a resource: an XML document with our list of buckets
        Response response = authorizedGet(getHost());
        DomRepresentation document = response.getEntityAsDom();

        // Use XPath to find the bucket names
        for (Node node : document.getNodes("//Bucket/Name")) {
            result.add(new S3Bucket(node.getTextContent()));
        }

        return result;
    }*/
}
